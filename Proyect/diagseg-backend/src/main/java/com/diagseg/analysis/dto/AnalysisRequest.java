// src/main/java/com/diagseg/analysis/dto/AnalysisRequest.java
package com.diagseg.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnalysisRequest {

    @NotBlank
    public String query;

    @NotNull
    public TargetType type;
}
