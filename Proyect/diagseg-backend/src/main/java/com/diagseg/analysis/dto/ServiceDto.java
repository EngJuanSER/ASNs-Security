// src/main/java/com/diagseg/analysis/dto/ServiceDto.java
package com.diagseg.analysis.dto;

import java.util.List;

public class ServiceDto {

    public int port;
    public Protocol protocol;
    public String service;
    public String version;
    public String banner;
    public List<String> vulnerabilities; // IDs de CVEs
    public RiskLevel riskLevel;
}
