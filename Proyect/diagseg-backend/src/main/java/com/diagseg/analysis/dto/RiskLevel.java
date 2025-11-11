// src/main/java/com/diagseg/analysis/dto/RiskLevel.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RiskLevel {
    @JsonProperty("low")
    LOW,

    @JsonProperty("medium")
    MEDIUM,

    @JsonProperty("high")
    HIGH
}
