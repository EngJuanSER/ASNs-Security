package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.RiskLevel;
import com.diagseg.analysis.dto.ServiceDto;
import com.diagseg.analysis.dto.VulnerabilityDto;
import com.diagseg.analysis.dto.VulnerabilitySeverity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SecurityScoringService {

    // -------------------- CONFIGURACIÓN DE PENALIZACIONES --------------------

    // Puertos de alto riesgo y sus penalizaciones
    private static final Map<Integer, Integer> HIGH_RISK_PORT_PENALTIES = new HashMap<>();

    // Puertos administrativos expuestos
    private static final Map<Integer, Integer> ADMIN_PORT_PENALTIES = new HashMap<>();

    static {
        // Puertos de alto riesgo
        HIGH_RISK_PORT_PENALTIES.put(23, 15);    // Telnet
        HIGH_RISK_PORT_PENALTIES.put(21, 10);    // FTP
        HIGH_RISK_PORT_PENALTIES.put(445, 8);    // SMB
        HIGH_RISK_PORT_PENALTIES.put(135, 8);    // RPC
        HIGH_RISK_PORT_PENALTIES.put(3389, 10);  // RDP
        HIGH_RISK_PORT_PENALTIES.put(5900, 10);  // VNC

        // Puertos administrativos
        ADMIN_PORT_PENALTIES.put(22, 5);         // SSH
        ADMIN_PORT_PENALTIES.put(3306, 8);       // MySQL
        ADMIN_PORT_PENALTIES.put(5432, 8);       // PostgreSQL
        ADMIN_PORT_PENALTIES.put(27017, 10);     // MongoDB
        ADMIN_PORT_PENALTIES.put(6379, 10);      // Redis
    }

    // -------------------- MÉTODOS PRINCIPALES --------------------

    /**
     * Calcula el securityScore (0-100) aplicando penalizaciones
     * a partir de los servicios y vulnerabilidades detectadas.
     */
    public int calculateScore(List<ServiceDto> services, List<VulnerabilityDto> vulnerabilities) {
        int score = 100;

        int servicePenalty = calculateServicePenalties(services);
        int vulnerabilityPenalty = calculateVulnerabilityPenalties(vulnerabilities);

        int totalPenalty = servicePenalty + vulnerabilityPenalty;

        score -= totalPenalty;
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        return score;
    }

    /**
     * Clasifica el nivel de riesgo con base en el score final.
     */
    public RiskLevel classifyRisk(int score) {
        if (score >= 80) return RiskLevel.LOW;
        if (score >= 60) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    // -------------------- DETALLE DE CÁLCULOS --------------------

    private int calculateServicePenalties(List<ServiceDto> services) {
        if (services == null || services.isEmpty()) {
            return 0;
        }

        int penalty = 0;
        int openPorts = services.size();

        for (ServiceDto service : services) {
            int port = service.port;

            // Penalización por puertos peligrosos
            if (HIGH_RISK_PORT_PENALTIES.containsKey(port)) {
                penalty += HIGH_RISK_PORT_PENALTIES.get(port);
            }

            // Penalización por puertos administrativos expuestos
            if (ADMIN_PORT_PENALTIES.containsKey(port)) {
                penalty += ADMIN_PORT_PENALTIES.get(port);
            }

            // Penalización leve por servicios sin versión
            if (service.version == null || service.version.isBlank()) {
                penalty += 1;
            }
        }

        // Penalizaciones por cantidad de puertos abiertos
        if (openPorts > 10) penalty += 5;
        if (openPorts > 20) penalty += 10;
        if (openPorts > 50) penalty += 15;

        return penalty;
    }

    private int calculateVulnerabilityPenalties(List<VulnerabilityDto> vulnerabilities) {
        if (vulnerabilities == null || vulnerabilities.isEmpty()) {
            return 0;
        }

        int criticalCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int lowCount = 0;

        for (VulnerabilityDto vuln : vulnerabilities) {
            VulnerabilitySeverity severity = vuln.severity;
            if (severity == null) continue;

            // Uso explícito del enum VulnerabilitySeverity
            switch (severity) {
                case CRITICAL -> criticalCount++;
                case HIGH -> highCount++;
                case MEDIUM -> mediumCount++;
                case LOW -> lowCount++;
            }
        }

        // Penalizaciones acumuladas con límites (según README)
        int criticalPenalty = Math.min(criticalCount * 15, 30); // -15 cada una, máx -30
        int highPenalty     = Math.min(highCount * 10, 30);     // -10 cada una, máx -30
        int mediumPenalty   = Math.min(mediumCount * 5, 20);    // -5 cada una, máx -20
        int lowPenalty      = Math.min(lowCount * 2, 10);       // -2 cada una, máx -10

        return criticalPenalty + highPenalty + mediumPenalty + lowPenalty;
    }
}
