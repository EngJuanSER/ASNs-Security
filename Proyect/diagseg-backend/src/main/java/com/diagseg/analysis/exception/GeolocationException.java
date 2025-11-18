// src/main/java/com/diagseg/analysis/exception/GeolocationException.java
package com.diagseg.analysis.exception;

public class GeolocationException extends RuntimeException {

    public GeolocationException(String message) {
        super(message);
    }

    public GeolocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
