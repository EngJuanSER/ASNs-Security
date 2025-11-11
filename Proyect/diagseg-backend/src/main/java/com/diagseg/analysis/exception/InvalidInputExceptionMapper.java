package com.diagseg.analysis.exception;

import com.diagseg.analysis.dto.ErrorResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {

    @Override
    public Response toResponse(InvalidInputException exception) {
        ErrorResponse body = new ErrorResponse(
            "IP inválida",
            exception.getMessage(),
            "INVALID_INPUT"
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
