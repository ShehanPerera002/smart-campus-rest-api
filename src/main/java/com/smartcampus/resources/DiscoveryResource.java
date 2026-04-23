package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint for the Smart Campus API.
 * 
 * This endpoint provides basic information about the API along with
 * links to available resources. It follows the HATEOAS principle,
 * allowing clients to discover endpoints dynamically.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    /**
     * Returns API metadata and available resource links.
     */
    @GET
    public Response getInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        // Basic API details
        info.put("apiName", "Smart Campus API");
        info.put("apiVersion", "1.0.0");
        info.put("description", "A REST API for managing rooms and sensors on campus.");
        info.put("status", "Running");

        // Contact details for support or reference
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("team", "Smart Campus Devs");
        contact.put("email", "support@smartcampus.uni.edu");
        info.put("contact", contact);

        // HATEOAS links to guide clients to available operations
        Map<String, Object> links = new LinkedHashMap<>();
        links.put("self", createLink("GET", "/api/v1", "Discovery endpoint"));
        links.put("rooms", createLink("GET", "/api/v1/rooms", "List all rooms"));
        links.put("addRoom", createLink("POST", "/api/v1/rooms", "Create a new room"));
        links.put("sensors", createLink("GET", "/api/v1/sensors", "List all sensors"));
        links.put("filterSensors", createLink("GET", "/api/v1/sensors?type={type}", "Filter sensors by type"));
        links.put("addSensor", createLink("POST", "/api/v1/sensors", "Register a new sensor"));
        links.put("sensorReadings", createLink("GET", "/api/v1/sensors/{id}/readings", "Get sensor reading history"));
        info.put("links", links);

        return Response.ok(info).build();
    }

    /**
     * Test endpoint used to trigger a server error (500).
     * This helps verify global exception handling.
     */
    @GET
    @Path("/test-error")
    public Response testError() {
        String s = null;
        s.length(); // Triggers NullPointerException
        return Response.ok().build();
    }

    /**
     * Helper method to create structured link objects for HATEOAS responses.
     */
    private Map<String, String> createLink(String method, String url, String desc) {
        Map<String, String> link = new LinkedHashMap<>();
        link.put("method", method);
        link.put("url", url);
        link.put("description", desc);
        return link;
    }
}