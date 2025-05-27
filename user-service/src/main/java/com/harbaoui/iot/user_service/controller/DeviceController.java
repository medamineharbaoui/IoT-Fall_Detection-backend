package com.harbaoui.iot.user_service.controller;

import com.harbaoui.iot.user_service.entity.Device;
import com.harbaoui.iot.user_service.service.DeviceService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * Add a new device for the authenticated user.
     */
@PostMapping
public ResponseEntity<?> addDevice(@RequestBody Device device, Principal principal) {
    try {
        Device savedDevice = deviceService.addDevice(principal.getName(), device);
        return ResponseEntity.ok(savedDevice);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}


    /**
     * Update a device (if owned by the authenticated user).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device, Principal principal) {
        Device updatedDevice = deviceService.updateDevice(id, device, principal.getName());
        return ResponseEntity.ok(updatedDevice);
    }

    /**
     * Delete a device 
     */
    @DeleteMapping("/{id}")
public ResponseEntity<?> deleteDevice(@PathVariable Long id, Principal principal) {
    try {
        deviceService.deleteDevice(id, principal.getName());
        return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
}

    /**
     * Get all devices belonging to the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<Device>> getDevices(Principal principal) {
        List<Device> devices = deviceService.getUserDevices(principal.getName());
        return ResponseEntity.ok(devices);
    }
/**
 * Public endpoint: Returns all device names in JSON format for Grafana.
 */
@GetMapping("/public-names")
public ResponseEntity<Map<String, List<Map<String, String>>>> getPublicDeviceNames() {
    Map<String, List<Map<String, String>>> result = deviceService.getAllDeviceNamesAsMap();
    return ResponseEntity.ok(result);
}


}
