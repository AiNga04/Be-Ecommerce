package com.zyna.dev.ecommerce.seeders;

import com.zyna.dev.ecommerce.authentication.models.Permission;
import com.zyna.dev.ecommerce.authentication.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        create("PRODUCT_READ", "Can view products");
        create("PRODUCT_WRITE", "Can create/update/delete products");

        create("USER_READ", "Can view users");
        create("USER_WRITE", "Can create/update/delete users");

        create("ROLE_MANAGE", "Can manage roles");
        create("PERMISSION_MANAGE", "Can manage permissions");
    }

    private void create(String name, String desc) {
        permissionRepository.findByName(name).orElseGet(() -> {
            Permission p = Permission.builder()
                    .name(name)
                    .description(desc)
                    .build();
            return permissionRepository.save(p);
        });
    }
}
