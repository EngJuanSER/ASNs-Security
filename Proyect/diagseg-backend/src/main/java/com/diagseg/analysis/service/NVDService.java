package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.VulnerabilityDto;
import com.diagseg.analysis.dto.VulnerabilitySeverity;
import com.diagseg.analysis.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para consultar la API de NVD (National Vulnerability Database)
 * Documentación API: https://nvd.nist.gov/developers/vulnerabilities
 */
@ApplicationScoped
public class NVDService {

    private static final Logger LOG = Logger.getLogger(NVDService.class);

    private static final String NVD_API_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";

    @ConfigProperty(name = "nvd.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "nvd.timeout-seconds", defaultValue = "30")
    int timeoutSeconds;

    @ConfigProperty(name = "nvd.results-per-page", defaultValue = "20")
    int resultsPerPage;

    @ConfigProperty(name = "nvd.cache-ttl-minutes", defaultValue = "60")
    int cacheTtlMinutes;

    private final HttpClient httpClient;

    public NVDService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Busca vulnerabilidades para un CPE específico
     *
     * @param cpe CPE en formato 2.3 (ej: cpe:2.3:a:apache:http_server:2.4.7:*:*:*:*:*:*:*)
     * @return Lista de vulnerabilidades encontradas
     */
    public List<VulnerabilityDto> searchByCpe(String cpe) {
        if (cpe == null || cpe.isBlank()) {
            LOG.warn("CPE vacío o nulo, no se puede buscar en NVD");
            return new ArrayList<>();
        }

        int retryCount = 0;
        int maxRetries = 2;

        while (retryCount <= maxRetries) {
            try {
                LOG.infof("Consultando NVD API para CPE: %s (intento %d/%d)", cpe, retryCount + 1, maxRetries + 1);

                // Construir URL con parámetros
                String encodedCpe = URLEncoder.encode(cpe, StandardCharsets.UTF_8);
                String url = String.format("%s?cpeName=%s&resultsPerPage=%d", 
                    NVD_API_URL, encodedCpe, resultsPerPage);

                // Construir request
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .GET();

                // Agregar API key si está configurada (mejora rate limits)
                apiKey.ifPresent(key -> requestBuilder.header("apiKey", key));

                HttpRequest request = requestBuilder.build();

                // Ejecutar request
                HttpResponse<String> response;
                try {
                    response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (java.net.http.HttpTimeoutException e) {
                    LOG.warnf("Timeout consultando NVD API (intento %d)", retryCount + 1);
                    if (retryCount == maxRetries) {
                        throw new ServiceException(
                            "La consulta de vulnerabilidades está tardando demasiado",
                            String.format("NVD API timeout después de %d segundos", timeoutSeconds),
                            "La base de datos de vulnerabilidades puede estar experimentando alta carga. Intente nuevamente en unos minutos.",
                            ServiceException.ErrorCode.NVD_TIMEOUT,
                            e
                        );
                    }
                    retryCount++;
                    Thread.sleep(2000 * retryCount); // Backoff exponencial
                    continue;
                } catch (java.net.ConnectException | java.net.UnknownHostException e) {
                    throw new ServiceException(
                        "No se pudo conectar al servicio de vulnerabilidades",
                        "Error de conexión con NVD API: " + e.getMessage(),
                        "Verifique su conexión a internet. El servicio NVD puede estar temporalmente no disponible.",
                        ServiceException.ErrorCode.NVD_CONNECTION_ERROR,
                        e
                    );
                }

                // Verificar status code
                if (response.statusCode() == 429) {
                    // Rate limit exceeded
                    String waitTimeHeader = response.headers().firstValue("Retry-After").orElse("60");
                    int waitSeconds = Integer.parseInt(waitTimeHeader);
                    
                    LOG.warnf("Rate limit excedido en NVD API. Retry-After: %d segundos", waitSeconds);
                    
                    String apiKeyMessage = apiKey.isEmpty() 
                        ? "Configure una API key de NVD para aumentar el límite de 5 a 50 peticiones por 30 segundos."
                        : "Ha alcanzado el límite de 50 peticiones por 30 segundos.";
                    
                    throw new ServiceException(
                        "Se ha excedido el límite de consultas al servicio de vulnerabilidades",
                        String.format("HTTP 429 - Rate limit excedido. Retry-After: %d segundos", waitSeconds),
                        String.format("Espere %d segundos antes de reintentar. %s", waitSeconds, apiKeyMessage),
                        ServiceException.ErrorCode.NVD_RATE_LIMIT
                    );
                }

                if (response.statusCode() == 503) {
                    throw new ServiceException(
                        "El servicio de vulnerabilidades no está disponible temporalmente",
                        "HTTP 503 - Service Unavailable de NVD API",
                        "El servicio NVD está en mantenimiento o experimentando problemas. Intente nuevamente en unos minutos.",
                        ServiceException.ErrorCode.NVD_CONNECTION_ERROR
                    );
                }

                if (response.statusCode() != 200) {
                    LOG.errorf("NVD API respondió con código: %d. Body: %s", 
                        response.statusCode(), response.body());
                    
                    // Para otros errores, devolver lista vacía en lugar de lanzar excepción
                    // (permite continuar el análisis sin vulnerabilidades)
                    LOG.warnf("Continuando sin datos de vulnerabilidades para CPE: %s", cpe);
                    return new ArrayList<>();
                }

                // Parsear respuesta JSON
                List<VulnerabilityDto> vulnerabilities = parseNvdResponse(response.body());
                LOG.infof("Vulnerabilidades encontradas para CPE %s: %d", cpe, vulnerabilities.size());

                return vulnerabilities;

            } catch (ServiceException e) {
                throw e; // Re-lanzar excepciones de servicio
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceException(
                    "La consulta fue interrumpida",
                    "InterruptedException durante retry de NVD: " + e.getMessage(),
                    "Intente realizar la consulta nuevamente",
                    ServiceException.ErrorCode.NVD_CONNECTION_ERROR,
                    e
                );
            } catch (Exception e) {
                LOG.errorf(e, "Error inesperado consultando NVD API para CPE: %s", cpe);
                // Devolver lista vacía para errores inesperados en lugar de fallar completamente
                return new ArrayList<>();
            }
        }

        return new ArrayList<>();
    }

    /**
     * Busca vulnerabilidades por múltiples criterios
     */
    public List<VulnerabilityDto> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }

        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = String.format("%s?keywordSearch=%s&resultsPerPage=%d", 
                NVD_API_URL, encodedKeyword, resultsPerPage);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .GET();

            apiKey.ifPresent(key -> requestBuilder.header("apiKey", key));

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOG.warnf("NVD keyword search falló con código: %d", response.statusCode());
                return new ArrayList<>();
            }

            return parseNvdResponse(response.body());

        } catch (Exception e) {
            LOG.errorf(e, "Error en keyword search de NVD: %s", keyword);
            return new ArrayList<>();
        }
    }

    /**
     * Parsea respuesta JSON de la API de NVD
     */
    private List<VulnerabilityDto> parseNvdResponse(String jsonResponse) {
        List<VulnerabilityDto> vulnerabilities = new ArrayList<>();

        try {
            JsonReader reader = Json.createReader(new StringReader(jsonResponse));
            JsonObject root = reader.readObject();

            // Verificar resultados totales
            int totalResults = root.getInt("totalResults", 0);
            if (totalResults == 0) {
                LOG.debug("NVD no encontró vulnerabilidades");
                return vulnerabilities;
            }

            // Obtener array de vulnerabilidades
            JsonArray vulnsArray = root.getJsonArray("vulnerabilities");
            if (vulnsArray == null || vulnsArray.isEmpty()) {
                return vulnerabilities;
            }

            LOG.debugf("Parseando %d vulnerabilidades del response NVD", vulnsArray.size());

            // Parsear cada vulnerabilidad
            for (int i = 0; i < vulnsArray.size(); i++) {
                try {
                    JsonObject vulnWrapper = vulnsArray.getJsonObject(i);
                    JsonObject cve = vulnWrapper.getJsonObject("cve");

                    VulnerabilityDto vuln = parseCveObject(cve);
                    if (vuln != null) {
                        vulnerabilities.add(vuln);
                    }
                } catch (Exception e) {
                    LOG.warnf(e, "Error parseando vulnerabilidad en índice %d", i);
                }
            }

        } catch (Exception e) {
            LOG.errorf(e, "Error parseando respuesta JSON de NVD");
        }

        return vulnerabilities;
    }

    /**
     * Parsea un objeto CVE del JSON de NVD
     */
    private VulnerabilityDto parseCveObject(JsonObject cve) {
        try {
            VulnerabilityDto vuln = new VulnerabilityDto();

            // ID del CVE
            vuln.id = cve.getString("id", "Unknown");

            // Descripción (en inglés)
            JsonArray descriptions = cve.getJsonArray("descriptions");
            if (descriptions != null && !descriptions.isEmpty()) {
                JsonObject desc = descriptions.getJsonObject(0);
                vuln.description = desc.getString("value", "No description available");
            } else {
                vuln.description = "No description available";
            }

            // Título (usamos ID si no hay descripción corta)
            vuln.title = vuln.id;

            // Métricas CVSS
            JsonObject metrics = cve.getJsonObject("metrics");
            if (metrics != null) {
                parseCvssMetrics(metrics, vuln);
            } else {
                // Sin métricas CVSS
                vuln.cvss = 0.0;
                vuln.severity = VulnerabilitySeverity.LOW;
            }

            // Referencias
            vuln.references = new ArrayList<>();
            JsonArray references = cve.getJsonArray("references");
            if (references != null) {
                for (int i = 0; i < Math.min(references.size(), 5); i++) { // Limitar a 5
                    JsonObject ref = references.getJsonObject(i);
                    String refUrl = ref.getString("url", null);
                    if (refUrl != null) {
                        vuln.references.add(refUrl);
                    }
                }
            }

            // Solución (placeholder, NVD no siempre tiene esto)
            vuln.solution = "Consultar con el vendor o aplicar parches disponibles. " +
                "Referencias: " + String.join(", ", vuln.references);

            return vuln;

        } catch (Exception e) {
            LOG.warnf(e, "Error parseando objeto CVE");
            return null;
        }
    }

    /**
     * Parsea métricas CVSS de un CVE
     */
    private void parseCvssMetrics(JsonObject metrics, VulnerabilityDto vuln) {
        // Priorizar CVSS v3.1
        JsonArray cvssV31 = metrics.getJsonArray("cvssMetricV31");
        if (cvssV31 != null && !cvssV31.isEmpty()) {
            JsonObject metric = cvssV31.getJsonObject(0);
            JsonObject cvssData = metric.getJsonObject("cvssData");

            vuln.cvss = cvssData.getJsonNumber("baseScore").doubleValue();
            String severityStr = cvssData.getString("baseSeverity", "LOW");
            vuln.severity = mapCvssSeverity(severityStr);
            return;
        }

        // Fallback a CVSS v3.0
        JsonArray cvssV30 = metrics.getJsonArray("cvssMetricV30");
        if (cvssV30 != null && !cvssV30.isEmpty()) {
            JsonObject metric = cvssV30.getJsonObject(0);
            JsonObject cvssData = metric.getJsonObject("cvssData");

            vuln.cvss = cvssData.getJsonNumber("baseScore").doubleValue();
            String severityStr = cvssData.getString("baseSeverity", "LOW");
            vuln.severity = mapCvssSeverity(severityStr);
            return;
        }

        // Fallback a CVSS v2.0
        JsonArray cvssV2 = metrics.getJsonArray("cvssMetricV2");
        if (cvssV2 != null && !cvssV2.isEmpty()) {
            JsonObject metric = cvssV2.getJsonObject(0);
            JsonObject cvssData = metric.getJsonObject("cvssData");

            vuln.cvss = cvssData.getJsonNumber("baseScore").doubleValue();
            // CVSS v2 usa diferente escala y nombres
            String severityStr = metric.getString("baseSeverity", "LOW");
            vuln.severity = mapCvssSeverity(severityStr);
            return;
        }

        // Sin métricas
        vuln.cvss = 0.0;
        vuln.severity = VulnerabilitySeverity.LOW;
    }

    /**
     * Mapea string de severidad CVSS a enum
     */
    private VulnerabilitySeverity mapCvssSeverity(String severity) {
        if (severity == null) {
            return VulnerabilitySeverity.LOW;
        }

        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> VulnerabilitySeverity.CRITICAL;
            case "HIGH" -> VulnerabilitySeverity.HIGH;
            case "MEDIUM" -> VulnerabilitySeverity.MEDIUM;
            case "LOW" -> VulnerabilitySeverity.LOW;
            default -> VulnerabilitySeverity.LOW;
        };
    }

    /**
     * Construye CPE 2.3 desde información de servicio
     * Usado cuando Nmap no proporciona CPE directamente
     */
    public String buildCpe23(String vendor, String product, String version) {
        if (vendor == null || product == null || version == null) {
            return null;
        }

        // Formato CPE 2.3: cpe:2.3:part:vendor:product:version:update:edition:language:sw_edition:target_sw:target_hw:other
        // part: a (application), o (operating system), h (hardware)
        // Los wildcards (*) indican "cualquier valor"
        
        return String.format("cpe:2.3:a:%s:%s:%s:*:*:*:*:*:*:*",
            vendor.toLowerCase().replace(" ", "_"),
            product.toLowerCase().replace(" ", "_"),
            version.toLowerCase());
    }

    /**
     * Verifica si el servicio NVD está disponible
     */
    public boolean healthCheck() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(NVD_API_URL + "?resultsPerPage=1"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            LOG.warnf("NVD health check falló: %s", e.getMessage());
            return false;
        }
    }
}
