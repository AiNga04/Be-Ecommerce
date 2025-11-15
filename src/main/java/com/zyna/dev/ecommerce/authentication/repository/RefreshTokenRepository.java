package com.zyna.dev.ecommerce.authentication.repository;

import com.zyna.dev.ecommerce.authentication.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
}
