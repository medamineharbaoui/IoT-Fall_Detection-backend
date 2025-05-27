package com.harbaoui.iot.user_service.repository;

import com.harbaoui.iot.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Update to return Optional<User> to use isPresent()
    Optional<User> findByEmail(String email); // example of a custom query
}
