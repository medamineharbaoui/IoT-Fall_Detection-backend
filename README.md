# IoT Fall Detection System - Backend & Embedded

## Overview
The backend of the IoT Fall Detection System is built using a **Spring Boot** microservices architecture with **Java 17**, utilizing **Hibernate** as the ORM framework for **MySQL** database integration. It comprises four services: Discovery Service, Gateway Service, User Service, and Sensor Data Service. The system integrates with an MQTT broker to collect real-time data from IoT devices, supporting the frontend’s real-time visualization with **Grafana**. The embedded component runs on an **ESP32** microcontroller, collecting sensor data and publishing it to the MQTT broker using C++ code.

## Backend Architecture

### 1. Discovery Service
- Implements a service registry (e.g., Eureka) for microservices discovery.
- Enables dynamic registration and discovery of services.
- Ensures scalability and fault tolerance in the microservices ecosystem.

### 2. Gateway Service
- Serves as the entry point for all client requests, routing them to appropriate microservices using **Spring Cloud Gateway**.
- Handles load balancing and API gateway features to support the frontend’s multi-page navigation and data requests.

### 3. User Service
- Manages user authentication and device operations:
  - **Authentication**:
    - **Register**: Creates user accounts with JWT token generation.
    - **Verify Email**: Sends verification emails via Google SMTP.
    - **Login**: Authenticates users and issues JWT tokens.
  - **Device Management**:
    - **Add**: Registers new IoT devices, ensuring device names match MQTT topics (e.g., `{device}/esp32/<device-name>`).
    - **Edit**: Updates device details.
    - **Delete**: Removes devices from the system.
- Uses **Hibernate** to map user and device entities to MySQL tables, handling CRUD operations.

### 4. Sensor Data Service
- Subscribes to MQTT topics (e.g., `{device}/esp32/sensors_data`, `{device}/esp32/presence`, `{device}/esp32/motion`, `{device}/esp32/fall`) to receive real-time data from IoT devices.
- Stores sensor data (temperature, humidity, presence, motion, fall distance, fall status) in a MySQL database using **Hibernate** for ORM.
- Provides RESTful APIs for the frontend to fetch historical data and supports Grafana’s real-time MQTT data streaming for the Real-Time Dashboard.

## Database
- **MySQL**: Stores user information, device details, and sensor data.
- **Hibernate**: Used as the ORM framework to map Java entities to MySQL tables, handling data persistence and queries.
- Schema includes tables for:
  - Users (for authentication).
  - Devices (for device management).
  - Sensor Data (for storing real-time and historical data).

## Embedded Component

### Overview
The embedded system runs on an **ESP32** microcontroller, collecting data from sensors and publishing it to an MQTT broker for real-time visualization in Grafana. It is developed using **PlatformIO** with **C++** and integrates libraries for sensor interfacing and communication.

### Hardware
- **ESP32 Microcontroller**: Core processing unit.
- **Sensors**:
  - **LD2410**: Radar sensor for presence and motion detection, including distance measurements for fall detection.
  - **DHT22**: Measures temperature and humidity.
  - **PIR Sensor**: Detects motion.
- **Display**: SSD1306 OLED (128x64) for real-time data visualization.
- **LED**: NeoPixel LED for status indication (red for fall detected, green for presence/motion).


### Code Explanation
The embedded code (`main.c++`) performs the following:
- **Initialization**:
  - Sets up serial communication (115200 baud), I2C for OLED (pins 6, 7), and initializes sensors (LD2410 on pins 18, 17; DHT22 on pin 8; PIR on pin 9) and NeoPixel LED (pin 48).
  - Configures the LD2410 radar in enhanced mode for precise distance measurements.
  - Establishes Wi-Fi and MQTT connections to the broker (`broker.hivemq.com`, port 1883).
- **Main Loop**:
  - Reads sensor data:
    - **LD2410**: Detects presence and moving target distance.
    - **DHT22**: Measures temperature and humidity.
    - **PIR**: Detects motion.
  - **Fall Detection Logic**:
    - Monitors distance changes within a 2-second window (`FALL_TIME_WINDOW`).
    - Triggers a fall detection if the distance change exceeds 30 cm (`FALL_DISTANCE_THRESHOLD`) and the current distance is below 250 cm (`FALL_HEIGHT_THRESHOLD`).
  - **Display**: Shows real-time sensor data (presence, motion, temperature, humidity, fall status, fall distance) on the OLED.
  - **LED Indication**:
    - Red: Fall detected.
    - Green: Presence or motion detected.
    - Red (default): No presence or motion.
  - **MQTT Publishing**:
    - Publishes boolean values (`presence`, `motion`, `fall`) as `1` or `0` to separate topics (`{device}/esp32/presence`, `{device}/esp32/motion`, `{device}/esp32/fall`).
    - Publishes numerical data (`temperature`, `humidity`, `fall_distance`) as JSON to `{device}/esp32/sensors_data`.
- **Error Handling**:
  - Retries Wi-Fi and MQTT connections if disconnected.
  - Displays initialization errors on the OLED and serial monitor, halting execution if initialization fails.

### Pin Configuration
- **LD2410**: RX on pin 18, TX on pin 17.
- **DHT22**: Connected to pin 8.
- **PIR**: Connected to pin 9.
- **OLED**: SDA on pin 6, SCL on pin 7.
- **NeoPixel**: Connected to pin 48.

### Fall Detection Parameters
- **Distance Threshold**: 30 cm (change in distance to detect a fall).
- **Time Window**: 2000 ms (time frame for distance change).
- **Height Threshold**: 250 cm (maximum distance for fall detection).

## Backend Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone <backend-repository-url>
   cd <backend-directory>
   ```

2. **Install Dependencies**:
   - Ensure **Java 17** and **Maven** are installed.
   - For each service (discovery, gateway, user, sensor-data):
     ```bash
     cd <service-directory>
     mvn install
     ```

3. **Configure Environment Variables**:
   - Create `application.yml` or `application.properties` files for each service in `src/main/resources/`.
   - Specify:
     - MySQL connection details with Hibernate configuration:
       ```yaml
       spring:
         datasource:
           url: jdbc:mysql://localhost:3306/iot_db
           username: root
           password: your_password
         jpa:
           hibernate:
             ddl-auto: update
           show-sql: true
           properties:
             hibernate:
               dialect: org.hibernate.dialect.MySQLDialect
       ```
     - MQTT broker details:
       ```yaml
       mqtt:
         broker: tcp://broker.hivemq.com:1883
         clientId: sensor-service-client
       ```
     - Google SMTP credentials for user-service:
       ```yaml
       spring:
         mail:
           host: smtp.gmail.com
           port: 587
           username: your-email@gmail.com
           password: your-app-password
       ```
     - JWT secret key:
       ```yaml
       jwt:
         secret: your_jwt_secret
       ```

4. **Set Up MySQL Database**:
   - Create a database named `iot_db`:
     ```sql
     CREATE DATABASE iot_db;
     ```
   - Hibernate’s `ddl-auto: update` will automatically create or update tables based on entity mappings. Alternatively, run provided SQL scripts for manual table creation.

5. **Run the Services**:
   - Start the discovery-service first:
     ```bash
     cd discovery-service
     mvn spring-boot:run
     ```
   - Then start gateway-service, user-service, and sensor-data-service in separate terminals:
     ```bash
     cd <service-directory>
     mvn spring-boot:run
     ```

6. **Verify Services**:
   - Discovery Service: Access the Eureka dashboard at `http://localhost:8761`.
   - Gateway Service: Routes requests via `http://localhost:8080`.

## Embedded Setup Instructions
1. **Set Up PlatformIO**:
   - Install PlatformIO in your IDE (e.g., VS Code).
   - Create a new project for ESP32.

2. **Install Libraries**:
   - Add the listed libraries to `platformio.ini`:
     ```ini
     [env:esp32dev]
     platform = espressif32
     board = esp32dev
     framework = arduino
     lib_deps =
         iavorvel/MyLD2410@^1.2.3
         adafruit/DHT sensor library@^1.4.6
         adafruit/Adafruit Unified Sensor@^1.1.15
         adafruit/Adafruit SSD1306@^2.5.13
         adafruit/Adafruit NeoPixel@^1.12.5
         knolleary/PubSubClient@^2.8
     ```

3. **Upload Code**:
   - Copy the provided `main.c++` code to your project.
   - Update Wi-Fi (`ssid`, `password`) and MQTT (`mqttServer`, `clientID`) settings in the code.
   - Upload the code to the ESP32 using PlatformIO:
     ```bash
     pio run --target upload
     ```

4. **Connect Hardware**:
   - Wire the sensors, OLED, and NeoPixel as per the pin configuration.
   - Power the ESP32 and verify sensor data on the OLED and MQTT broker (e.g., using MQTT Explorer).

## Usage
- **Backend**:
  - Access APIs via the gateway service (`http://localhost:8080`).
  - Register users and manage devices through the user-service endpoints, with data persisted using Hibernate.
  - Retrieve historical data via the sensor-data-service APIs, with real-time data streamed to Grafana via MQTT.
- **Embedded**:
  - The ESP32 continuously collects and publishes sensor data to the MQTT broker.
  - Monitor the OLED display for real-time data and fall detection status.
  - Verify data reception on the MQTT broker using a client to ensure Grafana integration.

## Future Enhancements
- **Backend**:
  - Add caching (e.g., Redis) for frequently accessed sensor data to reduce database load.

- **Embedded**:
  - Optimize power consumption for battery-operated deployments.
  - Add local storage (e.g., SD card) for data buffering during network outages.
  - Support multiple LD2410 sensors for multi-room monitoring.

