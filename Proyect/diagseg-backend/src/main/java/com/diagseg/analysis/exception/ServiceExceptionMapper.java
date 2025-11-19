package com.diagseg.analysis.exception;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Manejador global de excepciones que convierte ServiceException
 * en respuestas JSON amigables para el usuario
 */
@Provider
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    private static final Logger LOG = Logger.getLogger(ServiceExceptionMapper.class);

    @Override
    public Response toResponse(ServiceException exception) {
        
        // Log técnico completo para debugging
        LOG.errorf(exception, "ServiceException capturada - Code: %s, User Message: %s, Technical: %s", 
            exception.getErrorCode(), 
            exception.getUserMessage(),
            exception.getTechnicalDetails());
        
        // Construir respuesta JSON amigable para el usuario
        JsonObject errorResponse = Json.createObjectBuilder()
            .add("success", false)
            .add("error", exception.getUserMessage())
            .add("errorCode", exception.getErrorCode().toString())
            .add("suggestedAction", exception.getSuggestedAction())
            .add("technicalDetails", exception.getTechnicalDetails())
            .add("timestamp", System.currentTimeMillis())
            .build();
        
        // Determinar código HTTP según tipo de error
        Response.Status httpStatus = determineHttpStatus(exception.getErrorCode());
        
        return Response
            .status(httpStatus)
            .entity(errorResponse.toString())
            .build();
    }
    
    private Response.Status determineHttpStatus(ServiceException.ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_INPUT, ASN_INVALID_IP -> Response.Status.BAD_REQUEST;
            case NVD_RATE_LIMIT -> Response.Status.TOO_MANY_REQUESTS;
            case NMAP_TIMEOUT, NVD_TIMEOUT -> Response.Status.GATEWAY_TIMEOUT;
            case NVD_CONNECTION_ERROR, ASN_SERVICE_UNAVAILABLE -> Response.Status.SERVICE_UNAVAILABLE;
            case NMAP_PERMISSION_DENIED -> Response.Status.FORBIDDEN;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }
}
