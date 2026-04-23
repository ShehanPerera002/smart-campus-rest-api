package com.smartcampus.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logging filter for the API.
 * 
 * This filter captures all incoming requests and outgoing responses,
 * allowing basic monitoring of API activity. It helps with debugging
 * and improves visibility without adding logging code to each resource.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Logger used to record request and response details
    private static final Logger logger = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Log incoming HTTP request method and URI
        logger.info("Incoming Request: " +
                requestContext.getMethod() + " " +
                requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        // Log outgoing response status along with the request path
        logger.info("Outgoing Response: Status " +
                responseContext.getStatus() +
                " for " +
                requestContext.getUriInfo().getPath());
    }
}