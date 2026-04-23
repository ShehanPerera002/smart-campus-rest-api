package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Entry point of the Smart Campus REST API.
 * 
 * This class is responsible for starting the embedded Grizzly HTTP server
 * and loading the JAX-RS application configuration. It allows the API to run
 * independently without requiring an external application server.
 */
public class Main {

    // Logger used to output server status messages
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    // Base URI where the server will listen for incoming requests
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static void main(String[] args) throws IOException, InterruptedException {

        // Load the JAX-RS application configuration
        ResourceConfig config = new SmartCampusApplication();

        // Create and start the embedded Grizzly HTTP server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);

        // Log startup information for the user
        logger.info("Smart Campus API is starting up at http://localhost:8080/api/v1");
        logger.info("Use CTRL+C to shut it down.");

        // Add a shutdown hook to ensure the server stops cleanly when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down the server...");
            server.shutdownNow();
        }));

        // Keep the application running until it is manually stopped
        Thread.currentThread().join();
    }
}