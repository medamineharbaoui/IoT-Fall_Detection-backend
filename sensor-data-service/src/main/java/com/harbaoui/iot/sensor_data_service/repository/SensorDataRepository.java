package com.harbaoui.iot.sensor_data_service.repository;

import com.harbaoui.iot.sensor_data_service.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    // Find data by device name, sensor type, and date range
    List<SensorData> findByDeviceNameAndSensorTypeAndTimestampBetween(String deviceName, String sensorType, LocalDateTime startDate, LocalDateTime endDate);

    // Find data by device name and sensor type
    List<SensorData> findByDeviceNameAndSensorType(String deviceName, String sensorType);

    // Find data by device name and timestamp range
    List<SensorData> findByDeviceNameAndTimestampBetween(String deviceName, LocalDateTime startDate, LocalDateTime endDate);

    // Find data by device name
    List<SensorData> findByDeviceName(String deviceName);

    
}
