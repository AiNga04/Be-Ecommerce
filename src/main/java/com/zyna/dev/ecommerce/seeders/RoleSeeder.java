package com.zyna.dev.ecommerce.seeders;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class RoleSeeder implements CommandLineRunner {

    private final AppRoleRepository roleRepository;

    @Override
    public void run(String... args) {
        createRole("USER", "Default role for normal users");
        createRole("STAFF", "Staff role for handling orders and inventory");
        createRole("ADMIN", "Administrator with full permissions");
        createRole("SHIPPER", "Shipper role for delivery operations");
    }

    private void createRole(String code, String description) {
        roleRepository.findByCode(code).orElseGet(() -> {
            AppRole role = AppRole.builder()
                    .code(code)
                    .description(description)
                    .build();
            return roleRepository.save(role);
        });
    }
}
