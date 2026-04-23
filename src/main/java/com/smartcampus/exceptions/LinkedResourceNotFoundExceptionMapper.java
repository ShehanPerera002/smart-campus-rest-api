package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converts LinkedResourceNotFoundException into an HTTP 422 response.
 * 
 * This is used when the request is valid in structure, but refers to a resource
 * (such as a roomId) that does not exist in the system.
 * 
 * HTTP 422 is used instead of 404 because the request path is correct,
 * but the problem lies in the data provided within the request.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response.status(422) // Unprocessable Entity
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}