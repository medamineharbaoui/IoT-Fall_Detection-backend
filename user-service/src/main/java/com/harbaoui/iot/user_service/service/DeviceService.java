package com.harbaoui.iot.user_service.service;

import com.harbaoui.iot.user_service.entity.Device;
import com.harbaoui.iot.user_service.entity.User;
import com.harbaoui.iot.user_service.repository.DeviceRepository;
import com.harbaoui.iot.user_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Add a device to the user's device list
     */
    public Device addDevice(String username, Device device) {
    User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (deviceRepository.existsByNameAndUser(device.getName(), user)) {
        throw new IllegalArgumentException("You already have a device with this name.");
    }

    if (!device.getName().equals(device.getName()) &&
            deviceRepository.findByName(device.getName()).isPresent()) {
            throw new IllegalArgumentException("Device name already Taken, Try a different one.");
        }

    device.setUser(user);
    return deviceRepository.save(device);
}


    /**
     * Update a device if the user owns it
     */
    public Device updateDevice(Long deviceId, Device updatedDevice, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You do not own this device.");
        }

        // Check for name conflict if name is changed
        if (!device.getName().equals(updatedDevice.getName()) &&
            deviceRepository.findByName(updatedDevice.getName()).isPresent()) {
            throw new IllegalArgumentException("Device name already Taken, Try a different one.");
        }

        if (!device.getName().equals(updatedDevice.getName()) &&
        deviceRepository.existsByNameAndUser(updatedDevice.getName(), device.getUser())) {
        throw new IllegalArgumentException("You already have a device with this name.");
}


        device.setName(updatedDevice.getName());
        device.setDescription(updatedDevice.getDescription());

        return deviceRepository.save(device);
    }

    /**
     * Delete a device if the user owns it
     */
    public void deleteDevice(Long deviceId, String username) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        if (!device.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("You do not own this device.");
        }

        deviceRepository.delete(device);
    }

    /**
     * Get all devices owned by the user
     */
    public List<Device> getUserDevices(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user.getDevices();
    }

    public Map<String, List<Map<String, String>>> getAllDeviceNamesAsMap() {
    List<Map<String, String>> deviceList = deviceRepository.findAll().stream()
        .map(device -> Map.of("name", device.getName()))
        .toList();

    return Map.of("devices", deviceList);
}

   

}
