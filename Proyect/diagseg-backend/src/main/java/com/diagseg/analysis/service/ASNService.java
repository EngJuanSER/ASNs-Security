package com.diagseg.analysis.service;

import com.diagseg.analysis.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para obtener información de ASN (Autonomous System Number) y ISP
 * Utiliza servicios públicos gratuitos para complementar GeoLite2
 */
@ApplicationScoped
public class ASNService {

    private static final Logger LOG = Logger.getLogger(ASNService.class);

    // API pública gratuita para consultas ASN
    private static final String IPAPI_URL = "http://ip-api.com/json/";

    private final HttpClient httpClient;

    public ASNService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Información de ASN obtenida
     */
    public static class ASNInfo {
        public String asn;
        public String asnOrg;
        public String isp;

        public ASNInfo(String asn, String asnOrg, String isp) {
            this.asn = asn;
            this.asnOrg = asnOrg;
            this.isp = isp;
        }
    }

    /**
     * Obtiene información de ASN e ISP para una IP
     * 
     * @param ip Dirección IP a consultar
     * @return Información de ASN/ISP
     */
    public ASNInfo getASNInfo(String ip) {
        try {
            // Intentar primero con ip-api.com (gratuito, 45 req/min)
            ASNInfo info = queryIpApi(ip);
            if (info != null) {
                return info;
            }

            // Fallback: intentar con whois
            info = queryWhois(ip);
            if (info != null) {
                return info;
            }

            // Si todo falla, retornar valores por defecto
            LOG.warnf("No se pudo obtener información ASN para IP: %s", ip);
            return new ASNInfo("Unknown", "Unknown", "Unknown");

        } catch (Exception e) {
            LOG.errorf(e, "Error obteniendo ASN info para IP: %s", ip);
            return new ASNInfo("Unknown", "Unknown", "Unknown");
        }
    }

    /**
     * Consulta ip-api.com para obtener ASN/ISP
     */
    private ASNInfo queryIpApi(String ip) {
        try {
            String url = IPAPI_URL + ip + "?fields=status,as,isp,org";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (java.net.http.HttpTimeoutException e) {
                LOG.warnf("Timeout consultando ip-api.com para IP: %s", ip);
                // No lanzar excepción, solo retornar null para intentar fallback
                return null;
            } catch (java.net.ConnectException | java.net.UnknownHostException e) {
                LOG.warnf("Error de conexión con ip-api.com: %s", e.getMessage());
                // El servicio puede estar caído, usar fallback
                return null;
            }

            if (response.statusCode() == 429) {
                LOG.warnf("Rate limit alcanzado en ip-api.com (45 req/min)");
                // Informar al usuario pero no fallar completamente
                throw new ServiceException(
                    "El servicio de información de red está temporalmente sobrecargado",
                    "HTTP 429 de ip-api.com - Rate limit de 45 peticiones/minuto alcanzado",
                    "La información de ASN/ISP puede estar incompleta. Intente nuevamente en un minuto.",
                    ServiceException.ErrorCode.ASN_SERVICE_UNAVAILABLE
                );
            }

            if (response.statusCode() != 200) {
                LOG.warnf("ip-api.com respondió con código: %d", response.statusCode());
                return null;
            }

            // Parsear respuesta JSON simple
            String body = response.body();
            
            // Verificar si fue exitoso
            if (!body.contains("\"status\":\"success\"")) {
                LOG.debugf("ip-api.com no pudo resolver IP: %s", ip);
                return null;
            }

            // Extraer campos usando regex simple (evita dependencia de JSON library)
            String asn = extractJsonField(body, "as");
            String isp = extractJsonField(body, "isp");
            String org = extractJsonField(body, "org");

            // Formatear ASN
            if (asn != null && !asn.startsWith("AS")) {
                // Extraer solo el número si viene con descripción
                Pattern pattern = Pattern.compile("AS(\\d+)");
                Matcher matcher = pattern.matcher(asn);
                if (matcher.find()) {
                    asn = "AS" + matcher.group(1);
                }
            }

            LOG.debugf("ASN Info para %s: ASN=%s, ISP=%s, Org=%s", ip, asn, isp, org);

            return new ASNInfo(
                    asn != null ? asn : "Unknown",
                    org != null ? org : (isp != null ? isp : "Unknown"),
                    isp != null ? isp : "Unknown"
            );

        } catch (Exception e) {
            LOG.warnf(e, "Error consultando ip-api.com para IP: %s", ip);
            return null;
        }
    }

    /**
     * Extrae un campo de un JSON simple usando regex
     */
    private String extractJsonField(String json, String field) {
        try {
            Pattern pattern = Pattern.compile("\"" + field + "\":\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            LOG.debugf("No se pudo extraer campo '%s' del JSON", field);
        }
        return null;
    }

    /**
     * Consulta whois como fallback (menos confiable)
     */
    private ASNInfo queryWhois(String ip) {
        try {
            LOG.debugf("Intentando whois lookup para IP: %s", ip);

            ProcessBuilder pb = new ProcessBuilder("whois", ip);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String asn = null;
            String org = null;
            String isp = null;
            String line;

            while ((line = reader.readLine()) != null) {
                String lowerLine = line.toLowerCase();

                // Buscar ASN
                if (asn == null && (lowerLine.contains("origin:") || lowerLine.contains("originas:"))) {
                    Pattern pattern = Pattern.compile("AS(\\d+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        asn = "AS" + matcher.group(1);
                    }
                }

                // Buscar organización
                if (org == null && (lowerLine.contains("orgname:") || lowerLine.contains("org-name:"))) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        org = parts[1].trim();
                    }
                }

                // Buscar ISP/netname
                if (isp == null && lowerLine.contains("netname:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        isp = parts[1].trim();
                    }
                }
            }

            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
            }

            if (asn != null || org != null || isp != null) {
                return new ASNInfo(
                        asn != null ? asn : "Unknown",
                        org != null ? org : "Unknown",
                        isp != null ? isp : org
                );
            }

        } catch (Exception e) {
            LOG.debugf(e, "Error ejecutando whois para IP: %s", ip);
        }

        return null;
    }

    /**
     * Verifica si el servicio está disponible
     */
    public boolean healthCheck() {
        try {
            // Probar con IP pública conocida (Google DNS)
            ASNInfo info = queryIpApi("8.8.8.8");
            return info != null && !info.asn.equals("Unknown");
        } catch (Exception e) {
            return false;
        }
    }
}
