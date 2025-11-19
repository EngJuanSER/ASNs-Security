package com.diagseg.analysis.service;

import com.diagseg.analysis.exception.ServiceException;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DnsResolverService {

    private static final Logger LOG = Logger.getLogger(DnsResolverService.class);

    /**
     * Resuelve un dominio a su dirección IP.
     * Si el dominio tiene múltiples IPs (A records), devuelve la primera.
     * 
     * @param domain El dominio a resolver (ej: "google.com")
     * @return La dirección IP resuelta
     * @throws ServiceException Si no se puede resolver el dominio
     */
    public String resolveDomain(String domain) {
        try {
            LOG.infof("Resolviendo dominio: %s", domain);
            
            // Resolver el dominio usando DNS
            InetAddress address = InetAddress.getByName(domain);
            String resolvedIp = address.getHostAddress();
            
            LOG.infof("Dominio '%s' resuelto a IP: %s", domain, resolvedIp);
            return resolvedIp;
            
        } catch (UnknownHostException e) {
            LOG.errorf(e, "No se pudo resolver el dominio: %s", domain);
            throw new ServiceException(
                "No se pudo resolver el dominio '" + domain + "'",
                "Error DNS: " + e.getMessage(),
                "Verifica que el dominio sea válido y que tengas conexión a internet. " +
                "Intenta con: 'nslookup " + domain + "' o 'dig " + domain + "'",
                ServiceException.ErrorCode.INVALID_INPUT,
                e
            );
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado resolviendo dominio: %s", domain);
            throw new ServiceException(
                "Error inesperado al resolver el dominio",
                "Error: " + e.getMessage(),
                "Intenta nuevamente o contacta al administrador",
                ServiceException.ErrorCode.INTERNAL_ERROR,
                e
            );
        }
    }

    /**
     * Resuelve un dominio a todas sus direcciones IP asociadas.
     * Útil para dominios con múltiples A records (balanceo de carga).
     * 
     * @param domain El dominio a resolver
     * @return Lista de todas las IPs asociadas
     * @throws ServiceException Si no se puede resolver el dominio
     */
    public List<String> resolveAllAddresses(String domain) {
        try {
            LOG.infof("Resolviendo todas las direcciones para dominio: %s", domain);
            
            InetAddress[] addresses = InetAddress.getAllByName(domain);
            List<String> ips = new ArrayList<>();
            
            for (InetAddress addr : addresses) {
                ips.add(addr.getHostAddress());
            }
            
            LOG.infof("Dominio '%s' resuelto a %d direcciones IP", domain, ips.size());
            return ips;
            
        } catch (UnknownHostException e) {
            LOG.errorf(e, "No se pudo resolver el dominio: %s", domain);
            throw new ServiceException(
                "No se pudo resolver el dominio '" + domain + "'",
                "Error DNS: " + e.getMessage(),
                "Verifica que el dominio sea válido y que tengas conexión a internet",
                ServiceException.ErrorCode.INVALID_INPUT,
                e
            );
        }
    }

    /**
     * Valida si una cadena tiene formato de dominio válido.
     * 
     * @param domain Cadena a validar
     * @return true si parece un dominio válido
     */
    public boolean isValidDomainFormat(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        
        // Patrón básico para dominios
        // Permite: google.com, sub.domain.com, my-site.co.uk
        String domainPattern = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]?(\\.[a-zA-Z]{2,})+$";
        return domain.matches(domainPattern);
    }

    /**
     * Determina si una cadena es una IP o un dominio.
     * 
     * @param query La cadena a analizar
     * @return "ip" si es IP, "domain" si es dominio, "unknown" si no se puede determinar
     */
    public String detectType(String query) {
        if (query == null || query.isBlank()) {
            return "unknown";
        }
        
        query = query.trim();
        
        // IPv4 pattern
        if (query.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
            return "ip";
        }
        
        // IPv6 pattern (simplificado)
        if (query.matches("^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$")) {
            return "ip";
        }
        
        // Domain pattern
        if (isValidDomainFormat(query)) {
            return "domain";
        }
        
        return "unknown";
    }
}
