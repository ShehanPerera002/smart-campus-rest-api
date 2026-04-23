# Smart Campus Sensor & Room Management API

> **Module**: 5COSC022W – Client-Server Architectures  
> **University**: University of Westminster  
> **Technology**: JAX-RS + Java 23 + Embedded Grizzly

---

## Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Setup & Running](#setup--running)
5. [API Reference](#api-reference)
6. [curl Examples](#curl-examples)
7. [Report – Theory Question Answers](#report--theory-question-answers)
8. [Error Codes Reference](#error-codes-reference)

---

## Overview

This project implements a RESTful API designed for the management of smart campus environments, including rooms and their associated sensors. The system is developed using **pure JAX-RS** (Jersey 3) and hosted on an **embedded Grizzly server**, eliminating the need for a separate application container. To maintain reliable service while providing high performance, data is persisted in-memory using thread-safe **ConcurrentHashMap** structures, ensuring **data consistency** without the complexity of an external database.

Key features:
- **Discovery Endpoint**: Implements HATEOAS at the root URL for enhanced API discoverability.
- **Thread-safe Storage**: Utilizes `ConcurrentHashMap` to ensure data integrity and high performance under concurrent load.
- **Nested Resources**: Leverages sub-resource locators for the efficient management of hierarchical sensor data.
- **Standardized Error Handling**: Employs custom exception mappers to return uniform JSON responses while suppressing internal stack traces.
- **Global Observability**: A centralized filter logs all incoming requests and outgoing responses for comprehensive auditing.

---

## Architecture

```
Client  →  Grizzly Server (port 8080)
               │
               └──  JAX-RS (Jersey)
                         │
                    ┌────┴──────────────────────────┐
                    │           Filters             │
                    │        LoggingFilter          │
                    └────┬──────────────────────────┘
                         │
               ┌─────────┼──────────────┐
               │         │              │
         DiscoveryResource  RoomResource  SensorResource
                                             │
                                    SensorReadingResource
                        │
                    DataStore (Singleton)
```

---

## Project Structure

```
smart-campus-rest-api/
├── pom.xml                                # Maven configuration
└── src/main/java/com/smartcampus/
    ├── Main.java                          # Entry point (Starts Grizzly)
    ├── SmartCampusApplication.java        # JAX-RS ResourceConfig
    ├── datastore/
    │   └── DataStore.java                 # Singleton in-memory storage
    ├── models/
    │   ├── Room.java                      # Room data model
    │   ├── Sensor.java                    # Sensor data model
    │   └── SensorReading.java             # Reading data model
    ├── resources/
    │   ├── DiscoveryResource.java         # Root HATEOAS endpoint (/)
    │   ├── RoomResource.java              # Room management (/rooms)
    │   ├── SensorResource.java            # Sensor management (/sensors)
    │   └── SensorReadingResource.java     # Nested readings (/readings)
    ├── exceptions/
    │   ├── ErrorResponse.java             # Common error JSON model
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java # Returns 422
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java           # Returns 409
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java      # Returns 403
    │   └── GlobalExceptionMapper.java                 # Returns 500
    └── filters/
        └── LoggingFilter.java             # Request/Response logger
```

---

## Setup & Running

### Prerequisites
- Java 23 or higher
- Maven 3.x

---

### Option 1: Run using NetBeans (Recommended)

1. Open the project in NetBeans.
2. Ensure Maven dependencies are installed.
3. Locate the `Main.java` file.
4. Right-click → **Run File** (or press `Shift + F6`).

The server will start using the embedded Grizzly server.

---

### Option 2: Run using Command Line

#### Build the project

mvn clean package

#### Run the application

java -jar target/smart-campus-api-uber.jar


The API will be available at: **`http://localhost:8080/api/v1`**

-------

## API Reference

### Base URL: `http://localhost:8080/api/v1`

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| `GET` | `/` | API Metadata & HATEOAS Links | 200 |
| `GET` | `/rooms` | List all rooms (full objects) | 200 |
| `POST` | `/rooms` | Create a new room | 201 |
| `GET` | `/rooms/{id}` | Get specific room details | 200 / 404 |
| `DELETE` | `/rooms/{id}` | Delete a room (must be empty) | 204 / 404 / 409 |
| `GET` | `/sensors` | List all sensors (filter with `?type=X`) | 200 |
| `POST` | `/sensors` | Register a new sensor (linked to a room) | 201 / 422 |
| `GET` | `/sensors/{id}` | Get specific sensor details | 200 / 404 |
| `GET` | `/sensors/{id}/readings` | Get sensor reading history | 200 / 404 |
| `POST` | `/sensors/{id}/readings` | Submit a new reading | 201 / 403 / 404 |

---

## curl Examples (Windows CMD)

### 1. Discovery

curl -X GET http://localhost:8080/api/v1


### 2. Add a room

curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\":\"ROOM-A\",\"name\":\"Meeting Room\",\"capacity\":10}"


### 3. Add a sensor

curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"roomId\":\"ROOM-A\"}"


### 4. Filter sensors

curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"


### 5. Add a reading

curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d "{\"value\":22.5}"


---------

## Report – Theory Question Answers

### Part 1: Project & Application Configuration

#### Question 1.1: JAX-RS Resource Class Lifecycle
The lifecycle of resource classes in JAX-RS is request-scoped by default. This means that a new instance of the resource class is made for each incoming HTTP request and then destroyed after the response is sent. It doesn't use the same object over and over again, like a singleton.

This makes the code safer because each request works with its own object, so instance variables are not shared between requests that happen at the same time.

We can't store persistent data in the resource class itself, though, because any data stored there will be lost when the request is done.

To handle this, the Smart Campus API separates the resource layer from the data layer. A singleton DataStore is used, accessed through DataStore.getInstance(), so all requests share the same data source.

Thread safety is important because more than one thread can access this DataStore at the same time. However, a regular HashMap might cause problems with race conditions, which happen when two or more threads try to access and change the same data at the same time, which can lead to wrong or faulty information.

To fix this problem, the system has decided to use a ConcurrentHashMap, which is safe for threads and lets multiple threads read and write data at the same time.

#### Question 1.2: Hypermedia (HATEOAS)
A centralised discovery endpoint and dynamic URI construction enable hypermedia in the Smart Campus API. This is a more sophisticated form of RESTful design, also referred to Level 3 of the Richardson Maturity Model. Here, the API allows the users to navigate using links rather than being aware of all the endpoints.

The API does not rely only on external documentation, as it provides links in its responses, but contains links in its JSON responses that inform clients about what they can do and how to do it. This simplifies the API and increases its flexibility.

Dynamic discovery via the root endpoint is one of the key advantages. The DiscoveryResource returns a structured list of links, such as rooms, sensors and sensor readings. This implies that a client developer need not know anything but the base URL. By clicking on the links of the response, they are able to know all the other operations available. This renders the API self-documenting as it happens.

Loose coupling via dynamic URI construction is another benefit. UriInfo is employed to construct URIs dynamically (such as as getAbsolutePathBuilder) in the resource classes. This eliminates the hard coded URLs by clients. In the event that the API structure is altered (i.e. a new version or a different path), clients do not need to change as they use the links that the server gives them.

The API also allows you to communicate with it through a guided manner. A helper method creates links with the URL, the HTTP method and a brief description. This aids client developers in understanding how to utilize each endpoint properly that reduces errors and the necessity to seek information elsewhere.

---

### Part 2: Room Management

#### Question 2.1: Full Objects vs. IDs
In creating an API that will be used to provide a list of rooms, one has two primary options: to provide the room IDs only, or to provide the entire room objects. Both methods have varying implications on the network utilization and client-side computation. 

The idea of returning IDs only might be efficient in a network bandwidth perspective initially since the size of the response will be less. But in fact, this can result in increased network use in general. In the case the client requires more information like room name or capacity, it would be required to make individual requests on each room. 

This creates the so-called N + 1 request problem, in which a single request causes many other requests, and the overhead of the network is raised. Comparatively, the Smart Campus API provides complete room objects under the getAllRooms() call.

 Although it slightly increases the size of the initial response, it is more efficient in general, since all the necessary information (e.g. id, name, and capacity) is provided in a single request. This decreases the overall requests and enhances performance. On the client-side, the use of only IDs complicates the implementation. The client has to deal with several requests, asynchronous calls, and collate the results. It may also cause a poor user experience, as data is not shown immediately, but rather over time. The full objects given back make the client logic simpler. All the data required by the client is given in a single response and can be displayed instantly. This results in a more user-friendly experience and an easier state management.

#### Question 2.2: DELETE Idempotency
The DELETE command of the Smart Campus API is idempotent. An operation is said to be idempotent, when any repetitions of the same request lead to the same end-result on the server.

The deletion logic in the RoomResource is as follows:

When a DELETE request is made (such as DELETE /rooms/LIB-301), the system checks the DataStore. If the room is present and there are no sensors installed in it, the room is deleted, and the API returns an HTTP 204 No Content response.At this stage the server state is modified as the room is deleted.

In case the client repeats the same DELETE request, the system conducts the same check. But as the room has already been removed, it is no longer present in the DataStore. Consequently, the API sends out an HTTP 404 Not Found response.Although the responses are varying (204 on the first request, 404 on the further requests), the server state after both requests is the same, the room is not found. There are no other changes or side effects when the request is repeated.

This is also implemented with a safety check in case the room still has sensors, giving a 409 Conflict back. This has no impact on idempotency, as a request made multiple times in this state will always be responded to identically without altering the server.

---

### Part 3: Sensor Operations & Linking

#### Question 3.1: @Consumes Annotation
The Smart Campus API has a @Consumes(MediaType.APPLICATION_JSON) annotation is employed to make sure that API can receive only the JSON-formatted requests. When a client transmits data using an alternate format (text/plain or application/xml) the request is promptly rejected by the JAX-RS runtime. Upon receiving a request, the JAX-RS will first verify the Content-Type header of the client. It will then compare this value to the format specified in the annotation of the Consumes.

 In case of a mismatch the request is not forwarded to the resource method. Instead of continuing the request, the framework issues a immediate response of HTTP 415 Unsupported Media Type. This occurs prior to processing or conversion of the request body to Java objects. Consequently, the system will not undergo any unnecessary processing and will avoid any errors that might arise in case the server attempted to process something that was not supported. 

This behaviour enhances security and input validation as well. The API also uses the restricted format of JSON, which guarantees that all the incoming data is predictable and formatted properly. This minimizes the possibility of bad input and aids in avoiding bad data mapping and even possible injection issues.

#### Question 3.2: Query Parameters for Filtering
A path-based design like /api/v1/sensors/type/CO2 will consider the value of CO2 as an independent resource or a type. This implies a level of hierarchy with type being its own entity. Nevertheless, in the Smart Campus API, there is no type resource, it is merely a feature of a sensor. Due to this, the path is not the most suitable design to use in filtering.

It is more appropriate to use query parameters (e.g., /api/v1/sensors?type=CO2) and adhere to the rules of REST.

Semantic correctness is one of the reasons. In REST, the URL path typically represents a particular resource (e.g., /sensors/TEMP-001), whereas query parameters are used to select or search a collection. The value of using type=CO2 also indicates that the client wants the sensors collection with a filter.

Flexibility is another plus. The optional parameter in the SensorResource is the @QueryParam (type). This implies that a single endpoint can support both:

sensors → return all sensors
sensors?type=CO2 → return filtered sensors

Multiple endpoints or more complicated logic would be required had the path parameters been used.

There is also increased scalability of query parameters. Should additional filters be required in the future (such as status), they can be easily added such as:

/sensors?type=CO2&status=ACTIVE

The URL would be more complex, and difficult to handle with a path-based design.

Lastly, query parameters are standard API design, which simplifies the API to understand and use.In general, the @QueryParam usage leads to a cleaner, more flexible and more RESTful design since the identification of resources and filtering are explicitly separated.

---

### Part 4: Deep Nesting with Sub-Resources
#### Question 4.1: Sub-Resource Locator Pattern
Sub-Resource Locator pattern is applied in the SensorResource class in the Smart Campus API to process requests like /sensors/ Rather than operating readings within SensorResource, the method will create a new instance of the SensorReadingResource, which would carry out all the operations involving reading.

Separation of concerns is one of the primary advantages of this method. SensorResource class only handles sensors (creation and listing of sensors) whereas SensorReadingResource handles sensor readings. This simplifies the understanding, maintenance and debugging of the code as each class has a specific role to play.

The other benefit is that it prevents huge and cumbersome controller classes. In the event that all nested paths were processed in a single class, it would soon be hard to maintain as API expands. The logic is divided into smaller, manageable classes by means of sub-resource locators, which helps to maintain the code clean.

The context also is better handled in the pattern. SensorResource locator method can verify the sensorId and then forward it to SensorReadingResource. This is to make sure that the sub-resource has already acquired the required information and does not require performing the same checks in each method.

Moreover, it enhances reusability. SensorReadingResource is a distinct class, therefore it can be reused should other components of the API require sensor readings in the future.

---

### Part 5: Advanced Error Handling & Logging

#### Question 5.2: HTTP 422 vs. 404
The HTTP 404 Not Found status is commonly employed in a RESTful API when the endpoint itself is not found. In the example, when a client makes a request to /api/v1/sensors and gets a 404 response, it would have been assumed that the URL is wrong or the endpoint is not there. This can be misleading when the real issue is not the endpoint, but the information within the request.

Unprocessable Entity (422) is employed in the Smart Campus API when a client attempts to create a sensor with a roomid that does not exist. This is addressed with the LinkedResourceNotFoundExceptionMapper.

The reason 422 is more appropriate is that the request as such is valid. The server knows the request, endpoint is there and the format of the JSON is correct. The problem, however, lies in the data supplied - in the given case the roomId corresponds to a resource that is not available in the system.

The 422 is a good clue to show that there is an error in the request body that is semantic in nature, and not an endpoint issue. This simplifies the task of client developers to know what has gone wrong and how to correct it.

For example:

404 Not Found → the endpoint or URL is non-existent.
422 Unprocessable Entity → the endpoint is valid, but the data is invalid.

This solution is also compatible with the HTTP conventions, whereby 422 is sent when the server knows the request but is unable to act on it because of bad data.

#### Question 5.4: Cybersecurity Risks of Stack Traces
Raising internal Java stack traces to users of the API can pose a severe security threat. A stack trace may show information on how the system is constructed in detail, which may be beneficial to the developers but harmful in the wrong hands of attackers.

Information leakage is one of the significant risks. Stack traces are able to reveal the internal information like package names, class structure, and the organization of the application. This provides the attackers with the information about the design of the system and simplifies to find out its possible flaws.

They may display library and framework information also. To take the example, when the stack trace contains the information of using some libraries, an attacker can verify whether some versions contain known vulnerabilities and can potentially use them.

Moreover, stack traces can reveal method names, inner logic, and can reveal how the application works on data. This may assist an attacker in knowing the sensitivity of operations that are carried out and how to abuse them.

Stack traces can even expose database or server information, including file paths or internal settings, in some instances, which can further add to the risk of targeted attacks.

To avoid this, the Smart Campus API relies on a GlobalExceptionMapper. This manages the errors in a secure manner by:

Capturing the entire stack trace to debug it.
Sending a generic response (e.g. HTTP 500 Internal Server Error) to the client.

This guarantees that developers can continue to access the detailed information on errors, and external users can only receive minimal and safe responses.

#### Question 5.5: JAX-RS Filters
A cross-cutting concern in software design is a functionality that cuts across the whole application as opposed to being a component of a single business process. A good example is logging, as all API endpoints require it, but it does not directly relate to the functionality of the Smart Campus.

The logging is done using JAX-RS filters in the Smart Campus API rather than adding logging statements to each resource method.

Among them, there is a decrease in the level of code duplication. The logging logic is done once in a LoggingFilter, instead of in each method in each of the classes such as RoomResource and SensorResource, with logger.info() written in them all. This is according to the DRY (Don’t Repeat Yourself) principle.

Cleaner business logic is another advantage. In relocating logging to a filter, the resource classes are only concerned with their primary tasks, which could be generating sensors or removing rooms. This simplifies the code to read, maintain and test.

There are also filters that guarantee uniform logging. When writing down manually, one can easily forget to record some actions or have uneven formats. Using a global filter, all requests and responses are recorded in a standardized fashion, which makes a dependable audit trail.

Moreover, filters are easy to maintain. Should logging format require modification (adding timestamps or concealing confidential information), it can be performed in a single location without altering numerous resource classes.

Lastly, filters are more controllable and flexible. Logging may be switched on or off centrally and does not impact the business logic, and this can be useful in performance tuning in a variety of environments.

---

## Error Codes Reference

| Code | Name | Meaning in this API |
|------|------|---------------------|
| `200` | OK | Request completed successfully. |
| `201` | Created | Resource successfully created. |
| `204` | No Content | Request successful with no response body (e.g., deletion). |
| `400` | Bad Request | Invalid request due to missing fields or malformed JSON. |
| `403` | Forbidden | Sensor is in `MAINTENANCE` and cannot accept readings. |
| `404` | Not Found | The requested Room or Sensor ID does not exist. |
| `409` | Conflict | Cannot delete Room because it still contains sensors. |
| `415` | Unsupported Media Type | Request was not sent as `application/json`. |
| `422` | Unprocessable Entity | Request is valid but contains invalid data (e.g., referenced Room ID does not exist). |
| `500` | Internal Server Error | Unexpected server error (details hidden for security). |
