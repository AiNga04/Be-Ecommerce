package com.zyna.dev.ecommerce.authentication.repository;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByCode(String code);
    List<AppRole> findAllByCodeIn(Set<String> codes);
    boolean existsByCode(String code);

    @Override
    @EntityGraph(attributePaths = "permissions")
    List<AppRole> findAll();

    @Override
    @EntityGraph(attributePaths = "permissions")
    Optional<AppRole> findById(Long id);
}
