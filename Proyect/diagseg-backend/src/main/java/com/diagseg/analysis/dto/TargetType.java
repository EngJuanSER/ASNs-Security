// src/main/java/com/diagseg/analysis/dto/TargetType.java
package com.diagseg.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TargetType {
    @JsonProperty("ipv4")
    IPV4,

    @JsonProperty("ipv6")
    IPV6,

    @JsonProperty("asn")
    ASN
}
