package com.harbaoui.iot.sensor_data_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harbaoui.iot.sensor_data_service.entity.SensorData;
import com.harbaoui.iot.sensor_data_service.repository.SensorDataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SensorDataService {

    private final SensorDataRepository repo;
    private final Map<String, String> lastValues = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SensorDataService(SensorDataRepository repo) {
        this.repo = repo;
    }

    // Process incoming sensor data, storing it only if there's a change
    public void processSensorData(String topic, String payload) {
    String[] parts = topic.split("/");
    if (parts.length < 3) return;

    String deviceName = parts[0];     // e.g., harbaoui
    String sensorType = parts[2];     // e.g., sensors_data, motion, fall

    if ("sensors_data".equals(sensorType)) {
        try {
            Map<String, Object> jsonMap = objectMapper.readValue(payload, new TypeReference<>() {});
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                String cacheKey = deviceName + "/sensors_data/" + key;

                if (!value.equals(lastValues.get(cacheKey))) {
                    lastValues.put(cacheKey, value);
                    save(deviceName, key, value); // Save each key (e.g., temperature, humidity)
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Invalid JSON payload: " + payload);
        }
    } else {
        String cacheKey = deviceName + "/" + sensorType;

        if (!payload.equals(lastValues.get(cacheKey))) {
            lastValues.put(cacheKey, payload);
            save(deviceName, sensorType, payload); // Save single boolean-style value
        }
    }
}

    

    // Save new sensor data to the database
    private void save(String deviceName, String sensorType, String value) {
        SensorData data = new SensorData();
        data.setDeviceName(deviceName);
        data.setSensorType(sensorType);
        data.setValue(value);
        data.setTimestamp(LocalDateTime.now());
        repo.save(data);
    }

    // Fetch sensor data by deviceName, sensorType, and an optional date range (startDate, endDate)
    public List<SensorData> getSensorDataByCriteria(String deviceName, String sensorType, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            // Query the database with the provided date range
            return repo.findByDeviceNameAndSensorTypeAndTimestampBetween(deviceName, sensorType, startDate, endDate);
        } else {
            // Return all data if no date range is provided
            return repo.findByDeviceNameAndSensorType(deviceName, sensorType);
        }
    }

    // Fetch all sensor data for a device with an optional date range filter
    public List<SensorData> getAllDataByDeviceName(String deviceName, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            // Query the database with the provided date range
            return repo.findByDeviceNameAndTimestampBetween(deviceName, startDate, endDate);
        } else {
            // Return all data if no date range is provided
            return repo.findByDeviceName(deviceName);
        }
    }

    
}
