package com.diagseg.analysis.service;

import com.diagseg.analysis.dto.GeolocationDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeolocationService {

    public GeolocationDto resolve(String ip) {
        // TODO: integrar con GeoLite2 o BigQuery
        GeolocationDto geo = new GeolocationDto();
        geo.country = "United States";
        geo.countryCode = "US";
        geo.region = "California";
        geo.city = "Mountain View";
        geo.latitude = 37.4056;
        geo.longitude = -122.0775;
        geo.timezone = "America/Los_Angeles";
        geo.isp = "Google LLC";
        geo.asn = "AS15169";
        geo.org = "Google LLC";
        return geo;
    }
}
