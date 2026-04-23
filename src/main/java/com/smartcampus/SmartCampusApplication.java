package com.smartcampus;

import com.smartcampus.exceptions.*;
import com.smartcampus.filters.LoggingFilter;
import com.smartcampus.resources.DiscoveryResource;
import com.smartcampus.resources.RoomResource;
import com.smartcampus.resources.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 * @ApplicationPath sets the versioned base URI for all resources.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Register resource classes
        register(DiscoveryResource.class);
        register(RoomResource.class);
        register(SensorResource.class);

        // Register exception mappers
        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        // Register filters
        register(LoggingFilter.class);

        // JSON support via Jackson
        register(JacksonFeature.class);
    }
}
