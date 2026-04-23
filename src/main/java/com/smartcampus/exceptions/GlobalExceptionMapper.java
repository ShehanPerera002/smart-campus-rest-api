package com.smartcampus.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles unexpected errors across the API.
 * 
 * This acts as a global fallback to ensure that any unhandled exception
 * is converted into a consistent JSON response instead of exposing
 * internal stack traces to the client.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    // Logger used to record internal errors for debugging purposes
    private static final Logger logger = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {

        // Log the full error details on the server side
        logger.log(Level.SEVERE, "Internal server error: ", exception);

        // Return a generic message to the client for security reasons
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(
                        "Something went wrong on the server. Please try again later."))
                .build();
    }
}