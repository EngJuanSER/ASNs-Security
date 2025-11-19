package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.*;
import com.diagseg.analysis.exception.ServiceException;
import com.diagseg.analysis.validation.InputValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AnalysisService {

    private static final Logger LOG = Logger.getLogger(AnalysisService.class);

    @Inject
    InputValidator inputValidator;

    @Inject
    GeolocationService geolocationService;

    @Inject
    ASNService asnService;

    @Inject
    NmapService nmapService;

    @Inject
    NVDService nvdService;

    @Inject
    ReputationService reputationService;

    @Inject
    SecurityScoringService securityScoringService;

    @Inject
    RecommendationService recommendationService;

    @Inject
    DnsResolverService dnsResolverService;

    public AnalysisResult analyze(AnalysisRequest request) {
        // 1. Validar entrada
        inputValidator.validate(request);

        long start = System.currentTimeMillis();
        
        List<String> warnings = new ArrayList<>();
        List<String> partialFailures = new ArrayList<>();

        // 1.5. Si es un dominio, resolverlo a IP
        String targetIp = request.query;
        String originalDomain = null;
        
        if (request.type == TargetType.DOMAIN) {
            LOG.infof("Tipo DOMAIN detectado, resolviendo '%s' a IP", request.query);
            originalDomain = request.query;
            try {
                targetIp = dnsResolverService.resolveDomain(request.query);
                LOG.infof("Dominio '%s' resuelto a IP: %s", originalDomain, targetIp);
            } catch (ServiceException e) {
                LOG.errorf(e, "Error al resolver dominio '%s'", request.query);
                throw e; // Re-lanzar error crítico
            }
        }

        LOG.infof("Iniciando análisis para target: %s (IP: %s)", request.query, targetIp);

        // 2. Escaneo de servicios con Nmap (REAL) - CRÍTICO
        List<ServiceDto> services;
        try {
            services = nmapService.scanTarget(targetIp);
            LOG.infof("Escaneo Nmap completado: %d servicios encontrados", services.size());
        } catch (ServiceException e) {
            // Si Nmap falla, es un error crítico que debe propagarse
            LOG.errorf(e, "Error crítico en escaneo Nmap");
            throw e;
        }

        // 3. Obtener vulnerabilidades desde NVD para cada servicio (REAL) - NO CRÍTICO
        List<VulnerabilityDto> allVulnerabilities = new ArrayList<>();
        boolean nvdPartialFailure = false;

        for (ServiceDto service : services) {
            try {
                // Construir CPE para el servicio
                String cpe = nmapService.extractCpeForService(service.service, service.version);

                if (cpe != null && !cpe.isBlank()) {
                    // Consultar NVD con el CPE
                    List<VulnerabilityDto> vulns = nvdService.searchByCpe(cpe);

                    // Asociar CVE IDs al servicio
                    for (VulnerabilityDto vuln : vulns) {
                        if (!service.vulnerabilities.contains(vuln.id)) {
                            service.vulnerabilities.add(vuln.id);
                        }
                        
                        // Agregar a lista global si no está duplicado
                        boolean exists = allVulnerabilities.stream()
                                .anyMatch(v -> v.id.equals(vuln.id));
                        if (!exists) {
                            allVulnerabilities.add(vuln);
                        }
                    }

                    // Actualizar risk level del servicio basado en vulnerabilidades
                    if (!vulns.isEmpty()) {
                        double maxCvss = vulns.stream()
                                .mapToDouble(v -> v.cvss)
                                .max()
                                .orElse(0.0);

                        if (maxCvss >= 9.0) {
                            service.riskLevel = RiskLevel.HIGH;
                        } else if (maxCvss >= 7.0) {
                            service.riskLevel = RiskLevel.MEDIUM;
                        } else if (maxCvss >= 4.0 && service.riskLevel == RiskLevel.LOW) {
                            service.riskLevel = RiskLevel.MEDIUM;
                        }
                    }
                }
            } catch (ServiceException e) {
                // Si NVD falla (rate limit, timeout, etc), registrar pero continuar
                LOG.warnf(e, "Error consultando NVD para servicio %s:%d - continuando sin vulnerabilidades", 
                    service.service, service.port);
                nvdPartialFailure = true;
                partialFailures.add(String.format("Consulta de vulnerabilidades: %s", e.getUserMessage()));
                
                // No re-lanzar, permitir que el análisis continúe
            } catch (Exception e) {
                LOG.errorf(e, "Error inesperado procesando vulnerabilidades para servicio %s", service.service);
                // Continuar sin vulnerabilidades para este servicio
            }
        }

        if (nvdPartialFailure) {
            warnings.add("Algunas vulnerabilidades pueden no estar completas debido a limitaciones del servicio NVD");
        }

        // 4. Geolocalización con GeoLite2 (REAL) - NO CRÍTICO (usa fallback si falla)
        GeolocationDto geo;
        try {
            geo = geolocationService.resolve(targetIp);
            // Si todos los valores son "Unknown", agregar warning
            if ("Unknown".equals(geo.country) && "Unknown".equals(geo.city)) {
                warnings.add("No se pudo obtener geolocalización precisa - base de datos GeoLite2 no disponible");
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error en geolocalización, usando fallback");
            warnings.add("Error obteniendo geolocalización: " + e.getMessage());
            // Crear geo con valores por defecto
            geo = new GeolocationDto();
            geo.country = "Unknown";
            geo.countryCode = "--";
            geo.region = "Unknown";
            geo.city = "Unknown";
            geo.latitude = 0.0;
            geo.longitude = 0.0;
            geo.timezone = "UTC";
            geo.isp = "Unknown";
            geo.asn = "Unknown";
            geo.org = "Unknown";
        }

        // 5. Obtener información de ASN/ISP (REAL) - NO CRÍTICO
        try {
            ASNService.ASNInfo asnInfo = asnService.getASNInfo(targetIp);
            geo.asn = asnInfo.asn;
            geo.org = asnInfo.asnOrg;
            geo.isp = asnInfo.isp;
        } catch (ServiceException e) {
            LOG.warnf(e, "Error obteniendo ASN info - usando valores por defecto");
            warnings.add("Información de red (ASN/ISP) puede estar incompleta: " + e.getUserMessage());
            geo.asn = "Unknown";
            geo.org = "Unknown";
            geo.isp = "Unknown";
        } catch (Exception e) {
            LOG.warnf(e, "Error inesperado obteniendo ASN info");
            geo.asn = "Unknown";
            geo.org = "Unknown";
            geo.isp = "Unknown";
        }

        // 6. Reputación (basada en servicios y vulnerabilidades encontradas)
        List<ReputationSourceDto> reputation =
                reputationService.buildReputation(services, allVulnerabilities);

        // 7. Calcular score global y riskLevel
        int securityScore = securityScoringService.calculateScore(services, allVulnerabilities);
        RiskLevel riskLevel = securityScoringService.classifyRisk(securityScore);

        long now = System.currentTimeMillis();
        long duration = now - start;

        // 8. Generar recomendaciones
        List<RecommendationDto> recommendations =
                recommendationService.generateRecommendations(services, allVulnerabilities, securityScore);

        // 9. Metadatos actualizados
        AnalysisMetadataDto metadata = new AnalysisMetadataDto();
        metadata.scanDuration = duration;
        
        // Si se resolvió un dominio, incluir "dns" en las fuentes
        List<String> sources = new ArrayList<>();
        if (originalDomain != null) {
            sources.add("dns");
        }
        sources.addAll(List.of("nmap", "nvd", "geolite2", "ipapi"));
        metadata.sourcesUsed = sources;
        metadata.cached = false;
        
        // Agregar warnings si existen
        if (!warnings.isEmpty()) {
            metadata.warnings = warnings;
        }

        // 10. Resultado final
        AnalysisResult result = new AnalysisResult();
        result.ip = targetIp; // IP resuelta (o la IP original si no era dominio)
        result.domain = originalDomain; // Dominio original (null si era IP directamente)
        result.type = request.type;
        result.securityScore = securityScore;
        result.riskLevel = riskLevel;
        result.timestamp = now;
        result.services = services;
        result.geolocation = geo;
        result.reputation = reputation;
        result.vulnerabilities = allVulnerabilities;
        result.recommendations = recommendations;
        result.metadata = metadata;

        String logTarget = originalDomain != null ? originalDomain + " (" + targetIp + ")" : targetIp;
        LOG.infof("Análisis completado para %s en %d ms. Score: %d, Servicios: %d, Vulnerabilidades: %d, Warnings: %d",
            logTarget, duration, securityScore, services.size(), allVulnerabilities.size(), warnings.size());

        return result;
    }
}