package com.zyna.dev.ecommerce.authentication.controller;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final AppRoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRoleIfNotExists("USER", "Default role for normal users");
        createRoleIfNotExists("ADMIN", "Administrator with full permissions");
    }

    private void createRoleIfNotExists(String code, String description) {
        roleRepository.findByCode(code).orElseGet(() -> {
            AppRole role = AppRole.builder()
                    .code(code)
                    .description(description)
                    .build();
            return roleRepository.save(role);
        });
    }
}
