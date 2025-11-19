package com.diagseg.analysis.exception;

/**
 * Excepción base para errores de servicios de análisis
 */
public class ServiceException extends RuntimeException {
    
    private final String userMessage;
    private final String technicalDetails;
    private final String suggestedAction;
    private final ErrorCode errorCode;
    
    public ServiceException(String userMessage, String technicalDetails, 
                          String suggestedAction, ErrorCode errorCode) {
        super(userMessage);
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
        this.suggestedAction = suggestedAction;
        this.errorCode = errorCode;
    }
    
    public ServiceException(String userMessage, String technicalDetails, 
                          String suggestedAction, ErrorCode errorCode, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.technicalDetails = technicalDetails;
        this.suggestedAction = suggestedAction;
        this.errorCode = errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public String getTechnicalDetails() {
        return technicalDetails;
    }
    
    public String getSuggestedAction() {
        return suggestedAction;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public enum ErrorCode {
        NMAP_NOT_FOUND,
        NMAP_TIMEOUT,
        NMAP_PERMISSION_DENIED,
        NMAP_EXECUTION_ERROR,
        
        NVD_RATE_LIMIT,
        NVD_CONNECTION_ERROR,
        NVD_TIMEOUT,
        NVD_INVALID_RESPONSE,
        
        ASN_SERVICE_UNAVAILABLE,
        ASN_INVALID_IP,
        
        GEOLOCATION_DB_ERROR,
        GEOLOCATION_IP_NOT_FOUND,
        
        INVALID_INPUT,
        INTERNAL_ERROR
    }
}
