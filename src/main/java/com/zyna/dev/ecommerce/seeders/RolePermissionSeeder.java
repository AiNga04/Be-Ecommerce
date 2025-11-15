package com.zyna.dev.ecommerce.seeders;

import com.zyna.dev.ecommerce.authentication.models.AppRole;
import com.zyna.dev.ecommerce.authentication.models.Permission;
import com.zyna.dev.ecommerce.authentication.repository.AppRoleRepository;
import com.zyna.dev.ecommerce.authentication.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(3)
public class RolePermissionSeeder implements CommandLineRunner {

    private final AppRoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // Lấy ROLE ADMIN & USER
        AppRole admin = roleRepository.findByCode("ADMIN").orElse(null);
        AppRole user = roleRepository.findByCode("USER").orElse(null);

        if (admin == null || user == null) {
            return; // chưa có role thì thôi, tránh crash
        }

        // Gán FULL permissions cho ADMIN
        List<Permission> allPerms = permissionRepository.findAll();
        if (!allPerms.isEmpty()) {
            admin.setPermissions(new HashSet<>(allPerms));
            roleRepository.save(admin);
        }

        // Gán PRODUCT_READ cho USER (quyền xem product)
        permissionRepository.findByName("PRODUCT_READ").ifPresent(p -> {
            if (user.getPermissions() == null) {
                user.setPermissions(new HashSet<>());
            }
            user.getPermissions().add(p);
            roleRepository.save(user);
        });
    }
}
