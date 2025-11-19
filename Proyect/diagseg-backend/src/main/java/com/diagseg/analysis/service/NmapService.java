package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.Protocol;
import com.diagseg.analysis.dto.RiskLevel;
import com.diagseg.analysis.dto.ServiceDto;
import com.diagseg.analysis.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Servicio para ejecutar escaneos Nmap y parsear resultados XML
 */
@ApplicationScoped
public class NmapService {

    private static final Logger LOG = Logger.getLogger(NmapService.class);

    @ConfigProperty(name = "nmap.timeout", defaultValue = "60")
    int nmapTimeoutSeconds;

    @ConfigProperty(name = "nmap.ports", defaultValue = "22,80,443,8080,3306,5432,6379,27017")
    String defaultPorts;

    @ConfigProperty(name = "nmap.temp-dir", defaultValue = "/tmp")
    String tempDir;

    /**
     * Escanea una IP usando Nmap y devuelve lista de servicios detectados
     * 
     * @param target Dirección IP o hostname a escanear
     * @return Lista de servicios detectados con versiones
     */
    public List<ServiceDto> scanTarget(String target) {
        try {
            // Generar nombre único para archivo temporal
            String scanId = UUID.randomUUID().toString().substring(0, 8);
            String xmlOutputPath = tempDir + "/nmap_scan_" + scanId + ".xml";

            // Ejecutar Nmap
            LOG.infof("Iniciando escaneo Nmap para target: %s", target);
            boolean success = executeNmap(target, xmlOutputPath);

            if (!success) {
                LOG.warnf("Escaneo Nmap falló para target: %s", target);
                return new ArrayList<>();
            }

            // Parsear XML resultante
            List<ServiceDto> services = parseNmapXml(xmlOutputPath);
            LOG.infof("Escaneo completado. Servicios encontrados: %d", services.size());

            // Limpiar archivo temporal
            try {
                Files.deleteIfExists(Path.of(xmlOutputPath));
            } catch (Exception e) {
                LOG.warnf("No se pudo eliminar archivo temporal: %s", xmlOutputPath);
            }

            return services;

        } catch (Exception e) {
            LOG.errorf(e, "Error ejecutando escaneo Nmap para target: %s", target);
            return new ArrayList<>();
        }
    }

    /**
     * Ejecuta comando Nmap
     */
    private boolean executeNmap(String target, String outputPath) {
        try {
            // Construir comando Nmap
            // -sT: TCP connect scan (no requiere permisos de root, compatible con Railway)
            // -sV: detección de versiones
            // -p: puertos a escanear
            // --open: solo puertos abiertos
            // -oX: output en formato XML
            // -T4: timing template (más rápido pero detectable)
            String[] command = {
                "nmap",
                "-sT",  // TCP connect scan (sin raw sockets)
                "-sV",
                "-Pn",
                "-p", defaultPorts,
                "--open",
                "-T4",
                "-oX", outputPath,
                target
            };

            LOG.debugf("Ejecutando comando: %s", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process;
            
            try {
                process = pb.start();
            } catch (java.io.IOException e) {
                if (e.getMessage().contains("No such file")) {
                    throw new ServiceException(
                        "Nmap no está instalado en el sistema",
                        "El comando 'nmap' no se encontró en el PATH del sistema",
                        "Instale Nmap usando: 'sudo apt install nmap' (Ubuntu/Debian) o 'brew install nmap' (macOS)",
                        ServiceException.ErrorCode.NMAP_NOT_FOUND,
                        e
                    );
                }
                throw e;
            }

            // Capturar output para debugging
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Esperar con timeout
            boolean finished = process.waitFor(nmapTimeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                LOG.errorf("Timeout ejecutando Nmap después de %d segundos", nmapTimeoutSeconds);
                throw new ServiceException(
                    "El escaneo está tardando demasiado tiempo",
                    String.format("Nmap excedió el tiempo límite de %d segundos", nmapTimeoutSeconds),
                    "Intente nuevamente con menos puertos o aumente el timeout en la configuración",
                    ServiceException.ErrorCode.NMAP_TIMEOUT
                );
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String errorOutput = output.toString();
                LOG.errorf("Nmap falló con código: %d. Output: %s", exitCode, errorOutput);
                
                // Detectar error de permisos
                if (errorOutput.contains("Permission denied") || errorOutput.contains("Operation not permitted")) {
                    throw new ServiceException(
                        "Permisos insuficientes para ejecutar el escaneo",
                        String.format("Nmap requiere permisos especiales. Código de salida: %d", exitCode),
                        "Configure los permisos correctos para Nmap o ejecute el servicio con privilegios adecuados",
                        ServiceException.ErrorCode.NMAP_PERMISSION_DENIED
                    );
                }
                
                throw new ServiceException(
                    "Error al ejecutar el escaneo de puertos",
                    String.format("Nmap falló con código %d: %s", exitCode, errorOutput.substring(0, Math.min(200, errorOutput.length()))),
                    "Verifique que la dirección IP/dominio sea válida y accesible",
                    ServiceException.ErrorCode.NMAP_EXECUTION_ERROR
                );
            }

            // Verificar que se creó el archivo XML
            File xmlFile = new File(outputPath);
            if (!xmlFile.exists() || xmlFile.length() == 0) {
                LOG.errorf("No se generó archivo XML de salida: %s", outputPath);
                throw new ServiceException(
                    "El escaneo no generó resultados",
                    String.format("No se pudo crear o leer el archivo XML: %s", outputPath),
                    "Verifique los permisos de escritura en el directorio temporal",
                    ServiceException.ErrorCode.NMAP_EXECUTION_ERROR
                );
            }

            LOG.debugf("Nmap completado exitosamente. XML generado: %s", outputPath);
            return true;

        } catch (ServiceException e) {
            throw e; // Re-lanzar excepciones de servicio
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado ejecutando comando Nmap");
            throw new ServiceException(
                "Error inesperado durante el escaneo",
                "Excepción interna: " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                "Contacte al administrador del sistema si el problema persiste",
                ServiceException.ErrorCode.NMAP_EXECUTION_ERROR,
                e
            );
        }
    }

    /**
     * Parsea XML de Nmap y extrae servicios
     */
    private List<ServiceDto> parseNmapXml(String xmlPath) {
        List<ServiceDto> services = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(xmlPath));
            doc.getDocumentElement().normalize();

            // Navegar a <host> -> <ports> -> <port>
            NodeList hostList = doc.getElementsByTagName("host");
            if (hostList.getLength() == 0) {
                LOG.warn("No se encontró tag <host> en XML de Nmap");
                return services;
            }

            Element hostElement = (Element) hostList.item(0);
            NodeList portsList = hostElement.getElementsByTagName("port");

            LOG.debugf("Parseando %d puertos del XML", portsList.getLength());

            for (int i = 0; i < portsList.getLength(); i++) {
                Element portElement = (Element) portsList.item(i);

                try {
                    ServiceDto service = parsePort(portElement);
                    if (service != null) {
                        services.add(service);
                    }
                } catch (Exception e) {
                    LOG.warnf(e, "Error parseando puerto en índice %d", i);
                }
            }

        } catch (Exception e) {
            LOG.errorf(e, "Error parseando XML de Nmap: %s", xmlPath);
        }

        return services;
    }

    /**
     * Parsea un elemento <port> del XML de Nmap
     */
    private ServiceDto parsePort(Element portElement) {
        try {
            // Atributos del puerto
            int port = Integer.parseInt(portElement.getAttribute("portid"));
            String protocolStr = portElement.getAttribute("protocol");
            Protocol protocol = "udp".equalsIgnoreCase(protocolStr) ? Protocol.UDP : Protocol.TCP;

            // Estado del puerto
            NodeList stateList = portElement.getElementsByTagName("state");
            if (stateList.getLength() == 0) {
                return null; // Sin estado, puerto cerrado o filtrado
            }
            Element stateElement = (Element) stateList.item(0);
            String state = stateElement.getAttribute("state");

            if (!"open".equals(state)) {
                return null; // Solo puertos abiertos
            }

            // Información del servicio
            NodeList serviceList = portElement.getElementsByTagName("service");
            String serviceName = "unknown";
            String version = null;
            String banner = null;
            String cpe = null;

            if (serviceList.getLength() > 0) {
                Element serviceElement = (Element) serviceList.item(0);
                serviceName = serviceElement.getAttribute("name");
                
                String product = serviceElement.getAttribute("product");
                String serviceVersion = serviceElement.getAttribute("version");
                
                // Construir versión completa
                if (product != null && !product.isEmpty()) {
                    version = product;
                    if (serviceVersion != null && !serviceVersion.isEmpty()) {
                        version += " " + serviceVersion;
                    }
                }

                // Banner (extrainfo o tunnel)
                String extraInfo = serviceElement.getAttribute("extrainfo");
                String tunnel = serviceElement.getAttribute("tunnel");
                if (extraInfo != null && !extraInfo.isEmpty()) {
                    banner = extraInfo;
                } else if (tunnel != null && !tunnel.isEmpty()) {
                    banner = "tunnel: " + tunnel;
                }

                // CPE (Common Platform Enumeration)
                NodeList cpeList = serviceElement.getElementsByTagName("cpe");
                if (cpeList.getLength() > 0) {
                    cpe = cpeList.item(0).getTextContent();
                }
            }

            // Crear DTO
            ServiceDto service = new ServiceDto();
            service.port = port;
            service.protocol = protocol;
            service.service = serviceName;
            service.version = version != null ? version : "Unknown";
            service.banner = banner;
            service.vulnerabilities = new ArrayList<>(); // Se llenarán con NVDService
            service.riskLevel = calculateRiskLevel(port, serviceName);

            // Logging para debugging
            LOG.debugf("Puerto detectado: %d/%s - %s %s - CPE: %s", 
                port, protocolStr, serviceName, service.version, cpe);

            return service;

        } catch (Exception e) {
            LOG.warnf(e, "Error parseando elemento <port>");
            return null;
        }
    }

    /**
     * Calcula nivel de riesgo basado en puerto y servicio
     * Esto es una heurística simple, se refinará con datos de vulnerabilidades
     */
    private RiskLevel calculateRiskLevel(int port, String service) {
        // Puertos críticos con historial de vulnerabilidades
        if (port == 23 || port == 21 || port == 3389) { // Telnet, FTP, RDP
            return RiskLevel.HIGH;
        }

        // Bases de datos expuestas públicamente
        if (port == 3306 || port == 5432 || port == 27017 || port == 6379) {
            return RiskLevel.HIGH;
        }

        // Servicios comunes con riesgo moderado
        if (port == 22 || port == 80 || port == 8080) { // SSH, HTTP
            return RiskLevel.MEDIUM;
        }

        // HTTPS generalmente más seguro
        if (port == 443) {
            return RiskLevel.LOW;
        }

        // Otros puertos: riesgo medio por defecto
        return RiskLevel.MEDIUM;
    }

    /**
     * Extrae CPE de un servicio para búsqueda en NVD
     * (Se puede llamar desde NVDService para obtener el CPE del XML)
     */
    public String extractCpeForService(String serviceName, String version) {
        // Esta es una simplificación. En producción, se debería parsear
        // el CPE directamente del XML o construirlo usando base de datos de CPEs

        if (serviceName == null || version == null) {
            return null;
        }

        // Ejemplos de construcción de CPE 2.3
        if (serviceName.contains("ssh") && version.contains("OpenSSH")) {
            String versionNumber = version.replaceAll("[^0-9.]", "");
            return String.format("cpe:2.3:a:openbsd:openssh:%s:*:*:*:*:*:*:*", versionNumber);
        }

        if (serviceName.contains("http") && version.contains("Apache")) {
            String versionNumber = version.replaceAll("[^0-9.]", "");
            return String.format("cpe:2.3:a:apache:http_server:%s:*:*:*:*:*:*:*", versionNumber);
        }

        if (serviceName.contains("http") && version.contains("nginx")) {
            String versionNumber = version.replaceAll("[^0-9.]", "");
            return String.format("cpe:2.3:a:nginx:nginx:%s:*:*:*:*:*:*:*", versionNumber);
        }

        // Por ahora retornamos null para servicios no mapeados
        // En producción, se usaría una base de datos de mapeo servicio->CPE
        return null;
    }
}
