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

        AppRole admin = roleRepository.findByCode("ADMIN").orElse(null);
        AppRole staff = roleRepository.findByCode("STAFF").orElse(null);
        AppRole user = roleRepository.findByCode("USER").orElse(null);

        if (admin == null || staff == null || user == null) return;

        // ======== ADMIN ========
        List<Permission> allPerms = permissionRepository.findAll();
        admin.setPermissions(new HashSet<>(allPerms));
        roleRepository.save(admin);

        // ======== STAFF ========
        staff.setPermissions(new HashSet<>());

        addPerm(staff, "PRODUCT_READ");
        addPerm(staff, "INVENTORY_READ");
        addPerm(staff, "INVENTORY_WRITE");
        addPerm(staff, "ORDER_READ");
        addPerm(staff, "ORDER_MANAGE");

        roleRepository.save(staff);

        // ======== USER ========
        user.setPermissions(new HashSet<>());

        addPerm(user, "PRODUCT_READ");
        addPerm(user, "ORDER_READ"); // chỉ xem đơn của chính mình

        roleRepository.save(user);

        System.out.println(">>> Permissions assigned successfully.");
    }

    private void addPerm(AppRole role, String permName) {
        permissionRepository.findByName(permName).ifPresent(p -> {
            role.getPermissions().add(p);
        });
    }
}
