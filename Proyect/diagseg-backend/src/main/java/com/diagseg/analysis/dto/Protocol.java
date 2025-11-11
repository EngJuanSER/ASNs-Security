// src/main/java/com/diagseg/analysis/dto/Protocol.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Protocol {
    @JsonProperty("tcp")
    TCP,

    @JsonProperty("udp")
    UDP
}
