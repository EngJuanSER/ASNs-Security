package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.GeolocationDto;
import com.diagseg.analysis.exception.GeolocationException;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ApplicationScoped
public class GeolocationService {

    private static final Logger LOG = Logger.getLogger(GeolocationService.class);

    @ConfigProperty(name = "geolite2.city-db")
    String cityDbPath;

    private DatabaseReader dbReader;

    @PostConstruct
    void init() {
        try {
            InputStream dbStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(cityDbPath);

            if (dbStream == null) {
                LOG.warnf("⚠️ No se encontró la base de datos GeoLite2 en: %s - Usando fallback", cityDbPath);
                LOG.warn("Para obtener geolocalización completa, descargue GeoLite2-City.mmdb desde https://dev.maxmind.com/geoip/geolite2-free-geolocation-data");
                dbReader = null;
                return;
            }

            dbReader = new DatabaseReader.Builder(dbStream).build();
            LOG.infof("✅ GeoLite2 City DB cargada correctamente desde '%s'", cityDbPath);

        } catch (Exception e) {
            // Error al inicializar: no es crítico, usamos fallback
            LOG.warnf(e, "⚠️ Error inicializando GeoLite2 City DB, usando fallback");
            dbReader = null;
        }
    }

    public GeolocationDto resolve(String ip) {
        // Si no hay base de datos, usar fallback directamente
        if (dbReader == null) {
            LOG.debugf("GeoLite2 no disponible, usando fallback para IP '%s'", ip);
            return fallbackGeolocation();
        }
        
        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);

            GeolocationDto geo = new GeolocationDto();

            geo.country = safeGet(response.getCountry().getName(), "Unknown");
            geo.countryCode = safeGet(response.getCountry().getIsoCode(), "--");
            geo.region = safeGet(response.getMostSpecificSubdivision().getName(), "Unknown");
            geo.city = safeGet(response.getCity().getName(), "Unknown");

            if (response.getLocation() != null) {
                Double lat = response.getLocation().getLatitude();
                Double lon = response.getLocation().getLongitude();

                if (lat == null || lon == null) {
                    LOG.warnf("GeoLite2 devolvió lat/lon nulos para IP '%s'", ip);
                    geo.latitude = 0.0;
                    geo.longitude = 0.0;
                } else {
                    geo.latitude = lat;
                    geo.longitude = lon;
                }

                geo.timezone = safeGet(response.getLocation().getTimeZone(), "UTC");
            } else {
                LOG.warnf("GeoLite2 no tiene objeto Location para IP '%s'", ip);
                geo.latitude = 0.0;
                geo.longitude = 0.0;
                geo.timezone = "UTC";
            }

            // ISP/ASN/Org vendrán luego de BigQuery; por ahora placeholders
            geo.isp = "Unknown";
            geo.asn = "Unknown";
            geo.org = "Unknown";

            return geo;

        } catch (UnknownHostException e) {
            LOG.warnf("No se pudo resolver la IP '%s' en GeoLite2 (UnknownHost): %s", ip, e.getMessage());
            return fallbackGeolocation();
        } catch (GeoIp2Exception e) {
            LOG.errorf("Error GeoLite2 para IP '%s': %s", ip, e.getMessage());
            return fallbackGeolocation();
        } catch (Exception e) {
            LOG.errorf(e, "Error inesperado consultando GeoLite2 para IP '%s'", ip);
            return fallbackGeolocation();
        }
    }


    private GeolocationDto fallbackGeolocation() {
        GeolocationDto geo = new GeolocationDto();
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
        return geo;
    }

    private String safeGet(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
