package com.zyna.dev.ecommerce.authentication.repository;

import com.zyna.dev.ecommerce.authentication.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}