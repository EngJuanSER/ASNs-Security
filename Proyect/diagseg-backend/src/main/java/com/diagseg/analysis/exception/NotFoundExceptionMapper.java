package com.diagseg.analysis.exception;

import com.diagseg.analysis.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        ErrorResponse body = new ErrorResponse(
            "No se encontró información",
            exception.getMessage(),
            "NOT_FOUND"
        );

        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
