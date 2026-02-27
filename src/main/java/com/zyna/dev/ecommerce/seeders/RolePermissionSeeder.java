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
        AppRole shipper = roleRepository.findByCode("SHIPPER").orElse(null);

        if (admin == null || staff == null || user == null || shipper == null) return;

        // ================= ADMIN =================
        admin.setPermissions(new HashSet<>());

        addPerm(admin, "PRODUCT_READ");
        addPerm(admin, "PRODUCT_WRITE");
        addPerm(admin, "DASHBOARD_READ");

        addPerm(admin, "USER_READ");
        addPerm(admin, "USER_WRITE");

        addPerm(admin, "ROLE_MANAGE");
        addPerm(admin, "PERMISSION_MANAGE");

        addPerm(admin, "INVENTORY_READ");
        addPerm(admin, "INVENTORY_WRITE");

        addPerm(admin, "ORDER_READ");
        addPerm(admin, "ORDER_MANAGE");
        addPerm(admin, "SHIPPING_MANAGE");
        // KHÔNG cần ORDER_WRITE cho admin, để tránh tự đi đặt hàng 😄

        // VOUCHER: admin full quyền
        addPerm(admin, "VOUCHER_READ");
        addPerm(admin, "VOUCHER_WRITE");
        addPerm(admin, "VOUCHER_STATUS_MANAGE");
        addPerm(admin, "REVIEW_MANAGE");
        addPerm(admin, "SUPPORT_READ");
        addPerm(admin, "SUPPORT_WRITE");

        roleRepository.save(admin);

        // ================= STAFF =================
        staff.setPermissions(new HashSet<>());
        addPerm(staff, "USER_READ");

        addPerm(staff, "PRODUCT_READ");
        addPerm(staff, "DASHBOARD_READ");

        addPerm(staff, "INVENTORY_READ");
        addPerm(staff, "INVENTORY_WRITE");

        addPerm(staff, "ORDER_READ");
        addPerm(staff, "ORDER_MANAGE");
        addPerm(staff, "SHIPPING_MANAGE");

        // VOUCHER: staff được xem + bật/tắt, nhưng không sửa nội dung
        addPerm(staff, "VOUCHER_READ");
        addPerm(staff, "VOUCHER_STATUS_MANAGE");
        addPerm(staff, "REVIEW_MANAGE");
        addPerm(staff, "SUPPORT_READ");
        addPerm(staff, "SUPPORT_WRITE");

        roleRepository.save(staff);

        // ================= USER (shopper) =================
        user.setPermissions(new HashSet<>());

        addPerm(user, "PRODUCT_READ");
        addPerm(user, "ORDER_READ");   // xem lịch sử đơn của chính mình
        addPerm(user, "ORDER_WRITE");  // checkout, apply voucher

        // Không gán bất kỳ VOUCHER_* permission nào cho user

        roleRepository.save(user);

        // ================= SHIPPER =================
        shipper.setPermissions(new HashSet<>());
        addPerm(shipper, "SHIPPING_MANAGE");
        addPerm(shipper, "ORDER_READ"); // xem thông tin đơn khi giao
        roleRepository.save(shipper);

        System.out.println(">>> RolePermissionSeeder: permissions assigned.");
    }

    private void addPerm(AppRole role, String permName) {
        permissionRepository.findByName(permName).ifPresent(p -> {
            role.getPermissions().add(p);
        });
    }
}
