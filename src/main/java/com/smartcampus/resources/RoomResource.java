package com.smartcampus.resources;

import com.smartcampus.datastore.DataStore;
import com.smartcampus.exceptions.ErrorResponse;
import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.models.Room;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the /api/v1/rooms resource collection.
 *
 * <p>
 * JAX-RS creates a new instance of this class per HTTP request
 * (request-scoped).
 * Shared mutable state is held exclusively in the singleton {@link DataStore}
 * which
 * uses ConcurrentHashMap, making all operations thread-safe without explicit
 * locking.
 * </p>
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger logger = Logger.getLogger(RoomResource.class.getName());
    private final DataStore dataStore = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // GET all rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(dataStore.getRooms().values());
        logger.info("Fetching all rooms: found " + roomList.size());
        return Response.ok(roomList).build();
    }

    // POST a new room
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error: Room ID is missing."))
                    .build();
        }
        if (dataStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Error: Room '" + room.getId() + "' already exists."))
                    .build();
        }

        dataStore.saveRoom(room);
        logger.info("Created new room: " + room.getId());

        // Dynamic URI building for Location header
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        return Response.created(location).entity(room).build();
    }

    // GET a room by ID
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Room '" + roomId + "' not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    // DELETE a room
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);

        // Check if room exists
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Cannot delete: Room '" + roomId + "' does not exist."))
                    .build();
        }

        // Room must be empty of sensors to be deleted
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "' because it still has sensors assigned.");
        }

        dataStore.deleteRoom(roomId);
        logger.info("Deleted room: " + roomId);
        return Response.noContent().build();
    }
}

