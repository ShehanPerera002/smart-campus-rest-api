package com.smartcampus.resources;

import com.smartcampus.datastore.DataStore;
import com.smartcampus.exceptions.ErrorResponse;
import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Resource class responsible for managing sensor-related API operations.
 *
 * This class provides endpoints for listing sensors, retrieving a single sensor,
 * creating new sensors, and accessing nested reading resources.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // Logger used to record sensor-related API activity
    private static final Logger logger = Logger.getLogger(SensorResource.class.getName());

    // Shared in-memory data store
    private final DataStore dataStore = DataStore.getInstance();

    // UriInfo is used to build dynamic Location headers for newly created resources
    @Context
    private UriInfo uriInfo;

    /**
     * Returns all sensors in the system.
     *
     * A query parameter can be used to filter sensors by type
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(dataStore.getSensors().values());

        // Apply filtering only if a sensor type is provided
        if (type != null && !type.isBlank()) {
            sensorList = sensorList.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            logger.info("Listing sensors filtered by type: " + type);
        } else {
            logger.info("Listing all sensors: count=" + sensorList.size());
        }

        return Response.ok(sensorList).build();
    }

    /**
     * Retrieves a specific sensor using its ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Sensor '" + sensorId + "' not found."))
                    .build();
        }

        return Response.ok(sensor).build();
    }

    /**
     * Creates a new sensor and links it to an existing room.
     *
     * Validation is performed to ensure that the sensor ID is provided,
     * the sensor does not already exist, and the referenced roomId is valid.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error: Sensor ID is missing."))
                    .build();
        }

        if (dataStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Error: Sensor '" + sensor.getId() + "' already exists."))
                    .build();
        }

        // A sensor must always belong to a valid room
        String roomId = sensor.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error: roomId is required for sensors."))
                    .build();
        }

        Room room = dataStore.getRoom(roomId);

        // Return a validation error if the referenced room does not exist
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "The room '" + roomId + "' was not found. Please create it first.");
        }

        // Set a default sensor status if none is provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        dataStore.saveSensor(sensor);
        room.getSensorIds().add(sensor.getId());
        logger.info("Registered sensor: " + sensor.getId());

        // Build a dynamic URI for the newly created sensor
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        return Response.created(location).entity(sensor).build();
    }

    /**
     * Provides access to the nested reading resource for a specific sensor.
     *
     * This is an example of the sub-resource locator pattern.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId, uriInfo);
    }
}