package com.smartcampus.datastore;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class acts as an in-memory data store for the application.
 * 
 * A Singleton pattern is used so that the same instance is shared
 * across all API requests. This allows data to persist during runtime
 * without using a database.
 * 
 * ConcurrentHashMap is used to ensure thread-safe access when multiple
 * clients interact with the API at the same time.
 */
public class DataStore {

    // Logger for monitoring data operations
    private static final Logger logger = Logger.getLogger(DataStore.class.getName());

    // Singleton instance
    private static final DataStore INSTANCE = new DataStore();

    /**
     * Returns the shared DataStore instance.
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Data storage for rooms, sensors, and readings
    private final ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    /**
     * Private constructor prevents external instantiation.
     * Initial demo data is loaded for testing purposes.
     */
    private DataStore() {
        loadDemoData();
    }

    
    // -------ROOM OPERATIONS-------
    

    /**
     * Returns all rooms in the system.
     */
    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    /**
     * Retrieves a room by its ID.
     */
    public Room getRoom(String id) {
        return rooms.get(id);
    }

    /**
     * Saves a new room or updates an existing one.
     */
    public void saveRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    /**
     * Deletes a room by ID.
     */
    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    /**
     * Checks if a room exists.
     */
    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    
    // -------SENSOR OPERATIONS-------
    

    /**
     * Returns all sensors.
     */
    public ConcurrentHashMap<String, Sensor> getSensors() {
        return sensors;
    }

    /**
     * Retrieves a sensor by its ID.
     */
    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    /**
     * Saves a new sensor or updates an existing one.
     */
    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    /**
     * Checks if a sensor exists.
     */
    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    
    // -------READING OPERATIONS-------
    

    /**
     * Returns readings for a specific sensor.
     * If no readings exist, a new list is created.
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    /**
     * Adds a new reading to a sensor.
     */
    public void addReading(String sensorId, SensorReading reading) {
        getReadingsForSensor(sensorId).add(reading);
    }

    
    // -------DEMO DATA-------


    /**
     * Loads sample data so the API has initial content.
     * This helps when demonstrating the system.
     */
    private void loadDemoData() {

        // Create sample rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab A", 30);
        Room r3 = new Room("HALL-01", "Main Hall", 500);
        saveRoom(r1);
        saveRoom(r2);
        saveRoom(r3);

        // Create sample sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 450.0, "LIB-301");
        Sensor s3 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("OCC-001",  "Occupancy",   "ACTIVE", 12.0, "LAB-101");
        saveSensor(s1);
        saveSensor(s2);
        saveSensor(s3);
        saveSensor(s4);

        // Link sensors to rooms
        r1.getSensorIds().add("TEMP-001");
        r1.getSensorIds().add("CO2-001");
        r2.getSensorIds().add("TEMP-002");
        r2.getSensorIds().add("OCC-001");

        // Add initial readings
        addReading("TEMP-001", new SensorReading("READ-001", System.currentTimeMillis() - 60000, 21.5));
        addReading("CO2-001",  new SensorReading("READ-002", System.currentTimeMillis() - 30000, 450.0));

        logger.info("Demo data has been pre-loaded into the DataStore.");
    }
}