package com.diagseg.analysis;

import com.diagseg.analysis.dto.*;
import com.diagseg.analysis.validation.InputValidator;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.validation.Valid;

import java.util.List;

@Path("/api/analysis")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnalysisResource {

    @Inject
    InputValidator inputValidator;

    @POST
    @Path("/analyze")
    public AnalysisResult analyze(@Valid AnalysisRequest request) {

        // ✅ 1. Validar entrada
        inputValidator.validate(request);

        // ✅ 2. (Por ahora) devolver resultado dummy como antes
        long now = System.currentTimeMillis();

        AnalysisResult result = new AnalysisResult();
        result.ip = request.query;
        result.type = request.type;
        result.securityScore = 85;
        result.riskLevel = RiskLevel.LOW;
        result.timestamp = now;

        ServiceDto dnsService = new ServiceDto();
        dnsService.port = 53;
        dnsService.protocol = Protocol.UDP;
        dnsService.service = "dns";
        dnsService.version = "Google Public DNS";
        dnsService.banner = "DNS Server";
        dnsService.vulnerabilities = List.of();
        dnsService.riskLevel = RiskLevel.LOW;

        ServiceDto httpsService = new ServiceDto();
        httpsService.port = 443;
        httpsService.protocol = Protocol.TCP;
        httpsService.service = "https";
        httpsService.version = "nginx/1.18.0";
        httpsService.banner = "Server: nginx/1.18.0";
        httpsService.vulnerabilities = List.of("CVE-2021-23017");
        httpsService.riskLevel = RiskLevel.MEDIUM;

        result.services = List.of(dnsService, httpsService);

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
        result.geolocation = geo;

        ReputationSourceDto rep = new ReputationSourceDto();
        rep.source = "censys";
        rep.name = "Censys Dataset";
        rep.status = ReputationStatus.CLEAN;
        rep.statusText = "Clean";
        rep.score = 100;
        rep.details = "No malicious activity detected in scan history";
        rep.lastChecked = now;
        result.reputation = List.of(rep);

        VulnerabilityDto vuln = new VulnerabilityDto();
        vuln.id = "CVE-2021-23017";
        vuln.title = "nginx resolver DNS response";
        vuln.severity = VulnerabilitySeverity.MEDIUM;
        vuln.cvss = 5.6;
        vuln.description = "A security issue in nginx resolver was identified, which might allow an attacker who is able to forge UDP packets from the DNS server";
        vuln.solution = "Update nginx to version 1.20.1 or later";
        vuln.references = List.of(
            "https://nvd.nist.gov/vuln/detail/CVE-2021-23017",
            "https://nginx.org/en/security_advisories.html"
        );
        result.vulnerabilities = List.of(vuln);

        RecommendationDto rec1 = new RecommendationDto();
        rec1.title = "Actualizar nginx";
        rec1.description = "La versión actual de nginx (1.18.0) tiene vulnerabilidades conocidas. Actualizar a la versión 1.20.1 o superior.";
        rec1.priority = RecommendationPriority.HIGH;
        rec1.category = RecommendationCategory.SERVICE;

        RecommendationDto rec2 = new RecommendationDto();
        rec2.title = "Configurar Rate Limiting";
        rec2.description = "Implementar límites de tasa en el servidor DNS para prevenir ataques de amplificación DNS.";
        rec2.priority = RecommendationPriority.MEDIUM;
        rec2.category = RecommendationCategory.NETWORK;

        result.recommendations = List.of(rec1, rec2);

        AnalysisMetadataDto meta = new AnalysisMetadataDto();
        meta.scanDuration = 2341L;
        meta.sourcesUsed = List.of("censys", "geolite2");
        meta.cached = false;
        result.metadata = meta;

        return result;
    }
}
