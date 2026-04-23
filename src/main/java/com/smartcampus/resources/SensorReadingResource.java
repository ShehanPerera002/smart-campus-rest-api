package com.smartcampus.resources;

import com.smartcampus.datastore.DataStore;
import com.smartcampus.exceptions.ErrorResponse;
import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Resource class responsible for managing readings for a specific sensor.
 *
 * This sub-resource is reached through SensorResource and is used to
 * retrieve historical readings or add new readings for an individual sensor.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    // Logger used to record reading-related activity
    private static final Logger logger = Logger.getLogger(SensorReadingResource.class.getName());

    // ID of the sensor this resource is linked to
    private final String sensorId;

    // Shared in-memory data store
    private final DataStore dataStore = DataStore.getInstance();

    // UriInfo is used for building the Location header when a reading is created
    private final UriInfo uriInfo;

    // Creates a reading resource linked to a specific sensor.
     
    public SensorReadingResource(String sensorId, UriInfo uriInfo) {
        this.sensorId = sensorId;
        this.uriInfo  = uriInfo;
    }

    // Returns all readings recorded for the selected sensor.
     
    @GET
    public Response getReadings() {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Sensor '" + sensorId + "' not found."))
                    .build();
        }

        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        logger.info("Fetched readings for " + sensorId + ": count=" + readings.size());

        return Response.ok(readings).build();
    }

    // Returns a specific reading using its reading ID.
     
    @GET
    @Path("/{readingId}")
    public Response getReading(@PathParam("readingId") String readingId) {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Sensor '" + sensorId + "' not found."))
                    .build();
        }

        Optional<SensorReading> found = dataStore.getReadingsForSensor(sensorId).stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst();

        if (found.isPresent()) {
            return Response.ok(found.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Reading record '" + readingId + "' not found."))
                    .build();
        }
    }

    /**
     * Adds a new reading for the selected sensor.
     *
     * The sensor must be in an ACTIVE state before a new reading can be accepted.
     * When a reading is successfully added, the sensor's currentValue is also updated
     * to keep the latest state consistent.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Sensor '" + sensorId + "' not found."))
                    .build();
        }

        // Only active sensors are allowed to record new readings
        if (!sensor.isAvailable()) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is " + sensor.getStatus() +
                    " and cannot take new readings right now.");
        }

        // Generate a unique reading ID and store the current timestamp
        String readingId = "READ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        reading.setId(readingId);
        reading.setTimestamp(System.currentTimeMillis());

        // Save the reading in the datastore
        dataStore.addReading(sensorId, reading);

        // Update the current sensor value to reflect the latest reading
        sensor.setCurrentValue(reading.getValue());
        dataStore.saveSensor(sensor);

        logger.info("New reading for sensor " + sensorId + ": ID=" + readingId + ", Value=" + reading.getValue());

        // Build a dynamic URI for the created reading
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(readingId)
                .build();

        return Response.created(location).entity(reading).build();
    }
}