package com.harbaoui.iot.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.harbaoui.iot.user_service.entity.Device;
import com.harbaoui.iot.user_service.entity.User;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByName(String name);
    boolean existsByNameAndUser(String name, User user);

}
