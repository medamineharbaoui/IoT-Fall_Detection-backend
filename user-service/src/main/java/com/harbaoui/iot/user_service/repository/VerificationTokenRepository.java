package com.harbaoui.iot.user_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.harbaoui.iot.user_service.entity.User;
import com.harbaoui.iot.user_service.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);

}
