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

        // PRODUCT
        create("PRODUCT_READ", "Can view products");
        create("PRODUCT_WRITE", "Can create/update/delete products");

        // USER
        create("USER_READ", "Can view users");
        create("USER_WRITE", "Can create/update/delete users");

        // ROLE & PERMISSION
        create("ROLE_MANAGE", "Can manage roles");
        create("PERMISSION_MANAGE", "Can manage permissions");

        // INVENTORY
        create("INVENTORY_READ", "Can view stock and inventory logs");
        create("INVENTORY_WRITE", "Can adjust stock");

        // ORDERS
        create("ORDER_READ", "Can view orders");
        // dùng cho USER (shopper) để thêm giỏ & đặt hàng
        create("ORDER_WRITE", "Can place orders (checkout, cart)");
        // dùng cho STAFF / ADMIN để đổi trạng thái đơn
        create("ORDER_MANAGE", "Can update order status");

        // SHIPPING
        create("SHIPPING_MANAGE", "Can manage shipment lifecycle");

        // VOUCHERS (permission riêng)
        create("VOUCHER_READ", "Can view vouchers");
        create("VOUCHER_WRITE", "Can create/update voucher info");
        create("VOUCHER_STATUS_MANAGE", "Can change voucher status (activate/deactivate)");

        // REVIEWS
        create("REVIEW_MANAGE", "Can hide/unhide reviews");
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
