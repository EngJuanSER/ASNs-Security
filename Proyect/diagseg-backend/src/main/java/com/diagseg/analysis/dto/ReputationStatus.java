// src/main/java/com/diagseg/analysis/dto/ReputationStatus.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ReputationStatus {
    @JsonProperty("clean")
    CLEAN,

    @JsonProperty("suspicious")
    SUSPICIOUS,

    @JsonProperty("malicious")
    MALICIOUS,

    @JsonProperty("unknown")
    UNKNOWN
}
