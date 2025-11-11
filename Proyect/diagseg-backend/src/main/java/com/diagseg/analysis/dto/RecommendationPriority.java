// src/main/java/com/diagseg/analysis/dto/RecommendationPriority.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RecommendationPriority {
    @JsonProperty("low")
    LOW,

    @JsonProperty("medium")
    MEDIUM,

    @JsonProperty("high")
    HIGH
}
