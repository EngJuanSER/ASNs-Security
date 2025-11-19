// src/main/java/com/diagseg/analysis/dto/AnalysisMetadataDto.java
package com.diagseg.analysis.dto;

import java.util.List;

public class AnalysisMetadataDto {

    public long scanDuration;
    public List<String> sourcesUsed;
    public boolean cached;
    public List<String> warnings; // Mensajes de advertencia no críticos
}
