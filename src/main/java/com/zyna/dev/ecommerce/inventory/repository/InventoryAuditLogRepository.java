package com.zyna.dev.ecommerce.inventory.repository;

import com.zyna.dev.ecommerce.inventory.models.InventoryAuditLog;
import com.zyna.dev.ecommerce.products.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryAuditLogRepository extends JpaRepository<InventoryAuditLog, Long> {

    // Lấy theo product + khoảng thời gian (phục vụ filter + paging)
    Page<InventoryAuditLog> findByProductAndChangedAtBetweenOrderByChangedAtDesc(
            Product product,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    // Lấy tất cả audit theo khoảng thời gian (không filter theo product)
    Page<InventoryAuditLog> findByChangedAtBetweenOrderByChangedAtDesc(
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    // Dùng cho export: lấy full list không phân trang
    List<InventoryAuditLog> findByChangedAtBetweenOrderByChangedAtAsc(
            LocalDateTime from,
            LocalDateTime to
    );

    List<InventoryAuditLog> findByProductAndChangedAtBetweenOrderByChangedAtAsc(
            Product product,
            LocalDateTime from,
            LocalDateTime to
    );
}
