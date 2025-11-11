package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.RecommendationCategory;
import com.diagseg.analysis.dto.RecommendationDto;
import com.diagseg.analysis.dto.RecommendationPriority;
import com.diagseg.analysis.dto.ServiceDto;
import com.diagseg.analysis.dto.VulnerabilityDto;
import com.diagseg.analysis.dto.VulnerabilitySeverity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class RecommendationService {

    public List<RecommendationDto> generateRecommendations(List<ServiceDto> services,
                                                           List<VulnerabilityDto> vulnerabilities,
                                                           int securityScore) {

        List<RecommendationDto> recs = new ArrayList<>();

        List<ServiceDto> safeServices = services != null ? services : List.of();
        List<VulnerabilityDto> safeVulns = vulnerabilities != null ? vulnerabilities : List.of();

        // 1) VULNERABILIDADES -------------------------------------------------
        for (VulnerabilityDto v : safeVulns) {
            if (v == null || v.severity == null) {
                continue;
            }

            RecommendationDto r = new RecommendationDto();
            r.priority = priorityFromSeverity(v.severity);   // ← usa VulnerabilitySeverity
            r.category = RecommendationCategory.SERVICE;

            switch (v.severity) {
                case CRITICAL -> {
                    r.title = "Mitigar vulnerabilidad crítica " + v.id;
                    r.description = (v.solution != null && !v.solution.isBlank())
                            ? v.solution
                            : "La vulnerabilidad crítica " + v.id +
                              " debe mitigarse de inmediato. Actualizar el software afectado a " +
                              "una versión corregida y aplicar las recomendaciones del proveedor.";
                }
                case HIGH -> {
                    r.title = "Mitigar vulnerabilidad de severidad alta " + v.id;
                    r.description = (v.solution != null && !v.solution.isBlank())
                            ? v.solution
                            : "La vulnerabilidad " + v.id +
                              " tiene severidad alta. Planificar su corrección con prioridad, " +
                              "actualizando el servicio afectado y aplicando los parches disponibles.";
                }
                case MEDIUM -> {
                    r.title = "Revisar vulnerabilidad " + v.id;
                    r.description = (v.solution != null && !v.solution.isBlank())
                            ? v.solution
                            : "La vulnerabilidad " + v.id +
                              " tiene severidad media. Programar su mitigación en el corto plazo, " +
                              "priorizando sistemas expuestos a Internet.";
                }
                case LOW -> {
                    r.title = "Monitorear vulnerabilidad de bajo impacto " + v.id;
                    r.description = (v.solution != null && !v.solution.isBlank())
                            ? v.solution
                            : "La vulnerabilidad " + v.id +
                              " tiene impacto bajo. Monitorear su evolución y aplicar parches " +
                              "durante las ventanas de mantenimiento habituales.";
                }
            }

            recs.add(r);
        }

        // 2) PUERTOS INSEGUROS (Telnet / FTP) --------------------------------
        boolean hasTelnet = safeServices.stream().anyMatch(s -> s != null && s.port == 23);
        boolean hasFtp    = safeServices.stream().anyMatch(s -> s != null && s.port == 21);

        if (hasTelnet) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Desactivar Telnet (puerto 23)";
            r.description = "El puerto 23 (Telnet) está abierto. Telnet transmite credenciales " +
                    "y tráfico en texto plano, lo que lo hace altamente inseguro. " +
                    "Se recomienda deshabilitarlo y utilizar SSH como alternativa segura.";
            r.priority = RecommendationPriority.HIGH;
            r.category = RecommendationCategory.CONFIGURATION;
            recs.add(r);
        }

        if (hasFtp) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Migrar de FTP a FTPS/SFTP (puerto 21)";
            r.description = "El puerto 21 (FTP) está expuesto. FTP no cifra credenciales ni datos, " +
                    "lo cual facilita ataques de intercepción. Se recomienda migrar a FTPS o SFTP.";
            r.priority = RecommendationPriority.HIGH;
            r.category = RecommendationCategory.CONFIGURATION;
            recs.add(r);
        }

        // 3) SERVICIOS ADMINISTRATIVOS Y BBDD --------------------------------
        boolean hasRdp      = safeServices.stream().anyMatch(s -> s != null && s.port == 3389);
        boolean hasSsh      = safeServices.stream().anyMatch(s -> s != null && s.port == 22);
        boolean hasMysql    = safeServices.stream().anyMatch(s -> s != null && s.port == 3306);
        boolean hasPostgres = safeServices.stream().anyMatch(s -> s != null && s.port == 5432);
        boolean hasMongo    = safeServices.stream().anyMatch(s -> s != null && s.port == 27017);
        boolean hasRedis    = safeServices.stream().anyMatch(s -> s != null && s.port == 6379);

        if (hasRdp || hasSsh) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Restringir acceso administrativo remoto (SSH/RDP)";
            r.description = "Se detectaron servicios administrativos expuestos (SSH y/o RDP). " +
                    "Configurar reglas de firewall para limitar el acceso a rangos de IP confiables, " +
                    "habilitar autenticación fuerte (MFA, claves) y registrar intentos de acceso.";
            r.priority = RecommendationPriority.HIGH;
            r.category = RecommendationCategory.SECURITY;
            recs.add(r);
        }

        if (hasMysql || hasPostgres || hasMongo || hasRedis) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Proteger bases de datos expuestas a Internet";
            r.description = "Se detectaron servicios de base de datos accesibles desde Internet " +
                    "(MySQL, PostgreSQL, MongoDB o Redis). Restringir el acceso a redes internas o VPN, " +
                    "reforzar autenticación, cifrado y revisar que no existan credenciales débiles.";
            r.priority = RecommendationPriority.HIGH;
            r.category = RecommendationCategory.SECURITY;
            recs.add(r);
        }

        // 4) NÚMERO DE PUERTOS ABIERTOS (superficie de ataque) ---------------
        int openPorts = safeServices.size();
        if (openPorts > 10) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Reducir superficie de ataque (puertos abiertos)";

            StringBuilder desc = new StringBuilder();
            desc.append("Se detectaron ").append(openPorts)
                .append(" puertos/servicios expuestos a Internet. ");

            if (openPorts > 50) {
                desc.append("El número de servicios expuestos es muy elevado, lo que incrementa " +
                            "significativamente la superficie de ataque. ");
            } else if (openPorts > 20) {
                desc.append("Existe un número considerable de servicios expuestos. ");
            } else {
                desc.append("Podría haber servicios innecesarios expuestos. ");
            }

            desc.append("Revisar la lista de servicios y cerrar o filtrar aquellos " +
                        "que no sean estrictamente necesarios para la operación.");

            r.description = desc.toString();
            if (openPorts > 20) {
                r.priority = RecommendationPriority.HIGH;
            } else {
                r.priority = RecommendationPriority.MEDIUM;
            }
            r.category = RecommendationCategory.NETWORK;
            recs.add(r);
        }

        // 5) SCORE GLOBAL BAJO -----------------------------------------------
        if (securityScore < 60) {
            RecommendationDto r = new RecommendationDto();
            r.title = "Realizar revisión de seguridad completa";
            r.description = "El score de seguridad es bajo (" + securityScore + "). " +
                    "Se recomienda realizar una auditoría de seguridad completa: " +
                    "revisar configuraciones, endurecer servicios expuestos, actualizar software " +
                    "y corregir las vulnerabilidades detectadas.";
            r.priority = RecommendationPriority.HIGH;
            r.category = RecommendationCategory.SECURITY;
            recs.add(r);
        }

        // Quitar posibles duplicados por título
        return deduplicateByTitle(recs);
    }

    /**
     * Mapea VulnerabilitySeverity → prioridad de recomendación.
     * Esto usa explícitamente el enum VulnerabilitySeverity, así que
     * tu IDE ya no debería quejarse de que "no se usa".
     */
    private RecommendationPriority priorityFromSeverity(VulnerabilitySeverity severity) {
        return switch (severity) {
            case CRITICAL, HIGH -> RecommendationPriority.HIGH;
            case MEDIUM        -> RecommendationPriority.MEDIUM;
            case LOW           -> RecommendationPriority.LOW;
        };
    }

    private List<RecommendationDto> deduplicateByTitle(List<RecommendationDto> recs) {
        List<RecommendationDto> unique = new ArrayList<>();
        for (RecommendationDto r : recs) {
            if (r == null || r.title == null) continue;
            boolean exists = unique.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(x -> r.title.equals(x.title));
            if (!exists) {
                unique.add(r);
            }
        }
        return unique;
    }
}
