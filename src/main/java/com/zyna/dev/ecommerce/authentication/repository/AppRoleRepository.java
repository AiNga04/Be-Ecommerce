package com.zyna.dev.ecommerce.authentication.repository;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByCode(String code);
}