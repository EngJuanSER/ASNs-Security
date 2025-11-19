package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.ReputationSourceDto;
import com.diagseg.analysis.dto.ReputationStatus;
import com.diagseg.analysis.dto.ServiceDto;
import com.diagseg.analysis.dto.VulnerabilityDto;
import com.diagseg.analysis.dto.VulnerabilitySeverity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ReputationService {

    public List<ReputationSourceDto> buildReputation(List<ServiceDto> services,
                                                     List<VulnerabilityDto> vulnerabilities) {

        int score = 100;
        StringBuilder detailsBuilder = new StringBuilder();

        // 1. Penalización por servicios de riesgo
        long highRiskServices = services.stream()
                .filter(s -> s.riskLevel != null && s.riskLevel.name().equalsIgnoreCase("HIGH"))
                .count();

        long mediumRiskServices = services.stream()
                .filter(s -> s.riskLevel != null && s.riskLevel.name().equalsIgnoreCase("MEDIUM"))
                .count();

        if (highRiskServices > 0) {
            int penalty = (int) Math.min(30, highRiskServices * 10);
            score -= penalty;
            detailsBuilder.append("Servicios de alto riesgo detectados: ")
                    .append(highRiskServices)
                    .append(". Penalización: -").append(penalty).append(" puntos. ");
        }

        if (mediumRiskServices > 0) {
            int penalty = (int) Math.min(20, mediumRiskServices * 5);
            score -= penalty;
            detailsBuilder.append("Servicios de riesgo medio detectados: ")
                    .append(mediumRiskServices)
                    .append(". Penalización: -").append(penalty).append(" puntos. ");
        }

        // 2. Penalización por vulnerabilidades
        long criticalVulns = vulnerabilities.stream()
                .filter(v -> v.severity == VulnerabilitySeverity.CRITICAL)
                .count();

        long highVulns = vulnerabilities.stream()
                .filter(v -> v.severity == VulnerabilitySeverity.HIGH)
                .count();

        long mediumVulns = vulnerabilities.stream()
                .filter(v -> v.severity == VulnerabilitySeverity.MEDIUM)
                .count();

        if (criticalVulns > 0) {
            int penalty = (int) Math.min(30, criticalVulns * 15);
            score -= penalty;
            detailsBuilder.append("Vulnerabilidades críticas: ")
                    .append(criticalVulns)
                    .append(". Penalización: -").append(penalty).append(" puntos. ");
        }

        if (highVulns > 0) {
            int penalty = (int) Math.min(25, highVulns * 10);
            score -= penalty;
            detailsBuilder.append("Vulnerabilidades altas: ")
                    .append(highVulns)
                    .append(". Penalización: -").append(penalty).append(" puntos. ");
        }

        if (mediumVulns > 0) {
            int penalty = (int) Math.min(15, mediumVulns * 5);
            score -= penalty;
            detailsBuilder.append("Vulnerabilidades medias: ")
                    .append(mediumVulns)
                    .append(". Penalización: -").append(penalty).append(" puntos. ");
        }

        // 3. Normalizar score [0, 100]
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        // 4. Mapear score a status
        ReputationStatus status;
        String statusText;

        if (score >= 80) {
            status = ReputationStatus.CLEAN;
            statusText = "Clean";
            if (detailsBuilder.length() == 0) {
                detailsBuilder.append("No se detectó actividad maliciosa aparente en los servicios analizados.");
            }
        } else if (score >= 60) {
            status = ReputationStatus.SUSPICIOUS;
            statusText = "Suspicious";
            detailsBuilder.append(" Reputación degradada: actividad potencialmente riesgosa detectada.");
        } else {
            status = ReputationStatus.MALICIOUS;
            statusText = "Malicious";
            detailsBuilder.append(" Reputación severamente degradada: múltiples indicadores de compromiso.");
        }

        // Crear reputación basada en análisis de Nmap + NVD
        ReputationSourceDto scanRep = new ReputationSourceDto();
        scanRep.source = "nmap-nvd";
        scanRep.name = "Análisis de Servicios y Vulnerabilidades";
        scanRep.status = status;
        scanRep.statusText = statusText;
        scanRep.score = score;
        scanRep.details = detailsBuilder.toString();
        scanRep.lastChecked = System.currentTimeMillis();

        List<ReputationSourceDto> result = new ArrayList<>();
        result.add(scanRep);
        return result;
    }
}
