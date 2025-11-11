// src/main/java/com/diagseg/analysis/dto/RecommendationCategory.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RecommendationCategory {
    @JsonProperty("network")
    NETWORK,

    @JsonProperty("service")
    SERVICE,

    @JsonProperty("configuration")
    CONFIGURATION,

    @JsonProperty("security")
    SECURITY
}
