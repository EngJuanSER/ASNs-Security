// src/main/java/com/diagseg/analysis/dto/ErrorResponse.java
package com.diagseg.analysis.dto;

public class ErrorResponse {

    public String error;
    public String message;
    public String code;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, String code) {
        this.error = error;
        this.message = message;
        this.code = code;
    }
}
