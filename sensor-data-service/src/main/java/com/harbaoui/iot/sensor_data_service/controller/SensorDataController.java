package com.harbaoui.iot.sensor_data_service.controller;

import com.harbaoui.iot.sensor_data_service.entity.SensorData;
import com.harbaoui.iot.sensor_data_service.service.SensorDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensor-data")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    public SensorDataController(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    // Fetch data by device name, sensor type, and date range
    @GetMapping
    public ResponseEntity<Object> getSensorData(
            @RequestParam String deviceName,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate) {

        if (sensorType == null || sensorType.isEmpty()) {
            // If sensorType is empty, return all data for the deviceName
            List<SensorData> allData = sensorDataService.getAllDataByDeviceName(deviceName, startDate, endDate);
            if (allData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for device " + deviceName);
            }
            return ResponseEntity.ok(allData);
        } else {
            // Otherwise, fetch data based on deviceName, sensorType, and date range
            List<SensorData> filteredData = sensorDataService.getSensorDataByCriteria(deviceName, sensorType, startDate, endDate);
            if (filteredData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for device " + deviceName + " and sensor type " + sensorType);
            }
            return ResponseEntity.ok(filteredData);
        }
    }
}
