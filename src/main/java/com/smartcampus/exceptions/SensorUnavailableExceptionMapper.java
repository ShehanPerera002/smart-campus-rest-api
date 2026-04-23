package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converts SensorUnavailableException into an HTTP 403 response.
 * 
 * This occurs when a sensor exists, but cannot accept new readings
 * because it is in a state such as MAINTENANCE or OFFLINE.
 * 
 * HTTP 403 (Forbidden) is used because the request is valid,
 * but the operation is not allowed due to the current state.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}