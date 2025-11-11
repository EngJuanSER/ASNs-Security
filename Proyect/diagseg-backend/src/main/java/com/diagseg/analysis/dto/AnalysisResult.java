// src/main/java/com/diagseg/analysis/dto/AnalysisResult.java
package com.diagseg.analysis.dto;

import java.util.List;

public class AnalysisResult {

    public String ip;
    public TargetType type;
    public int securityScore;
    public RiskLevel riskLevel;
    public long timestamp;

    public List<ServiceDto> services;
    public GeolocationDto geolocation;
    public List<ReputationSourceDto> reputation;
    public List<VulnerabilityDto> vulnerabilities;
    public List<RecommendationDto> recommendations;
    public AnalysisMetadataDto metadata;
}
