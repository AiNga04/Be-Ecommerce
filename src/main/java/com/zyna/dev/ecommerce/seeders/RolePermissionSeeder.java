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
        admin.setPermissions(new HashSet<>());

        addPerm(admin, "PRODUCT_READ");
        addPerm(admin, "PRODUCT_WRITE");

        addPerm(admin, "USER_READ");
        addPerm(admin, "USER_WRITE");

        addPerm(admin, "ROLE_MANAGE");
        addPerm(admin, "PERMISSION_MANAGE");

        addPerm(admin, "INVENTORY_READ");
        addPerm(admin, "INVENTORY_WRITE");

        addPerm(admin, "ORDER_READ");
        addPerm(admin, "ORDER_MANAGE");
        // KHÔNG add ORDER_WRITE => admin không thể checkout

        roleRepository.save(admin);

        // ======== STAFF ========
        staff.setPermissions(new HashSet<>());

        addPerm(staff, "PRODUCT_READ");
        addPerm(staff, "INVENTORY_READ");
        addPerm(staff, "INVENTORY_WRITE");

        addPerm(staff, "ORDER_READ");
        addPerm(staff, "ORDER_MANAGE");  // chỉ xử lý trạng thái đơn

        roleRepository.save(staff);

        // ======== USER (shopper) ========
        user.setPermissions(new HashSet<>());

        addPerm(user, "PRODUCT_READ");
        addPerm(user, "ORDER_READ");   // xem lịch sử đơn của chính mình
        addPerm(user, "ORDER_WRITE");  // chỉ user được quyền add cart + checkout

        roleRepository.save(user);

        System.out.println(">>> Permissions assigned successfully.");
    }

    private void addPerm(AppRole role, String permName) {
        permissionRepository.findByName(permName).ifPresent(p -> {
            role.getPermissions().add(p);
        });
    }
}
