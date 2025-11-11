package com.diagseg.analysis.exception;

import com.diagseg.analysis.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // Opcional: loggear el error
        exception.printStackTrace();

        ErrorResponse body = new ErrorResponse(
            "Error del servidor",
            "Ocurrió un error al procesar la solicitud. Por favor intente nuevamente",
            "INTERNAL_ERROR"
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
