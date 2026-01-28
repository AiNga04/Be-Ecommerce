package com.zyna.dev.ecommerce.inventory.service.impl;

import com.lowagie.text.Font;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.notifications.NotificationService;
import com.zyna.dev.ecommerce.notifications.NotificationType;
import com.zyna.dev.ecommerce.inventory.InventoryMapper;
import com.zyna.dev.ecommerce.inventory.dto.request.AdjustStockRequest;
import com.zyna.dev.ecommerce.inventory.dto.response.InventoryAuditResponse;
import com.zyna.dev.ecommerce.inventory.models.InventoryAuditLog;
import com.zyna.dev.ecommerce.inventory.repository.InventoryAuditLogRepository;
import com.zyna.dev.ecommerce.inventory.service.interfaces.InventoryService;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.products.repository.ProductRepository;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

// Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// PDF
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryAuditLogRepository auditLogRepository;
    private final InventoryMapper inventoryMapper;
    private final NotificationService notificationService;

    @Value("${app.inventory.low-stock.threshold:5}")
    private int lowStockThreshold;

    // Lấy user hiện tại từ SecurityContext
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApplicationException(HttpStatus.UNAUTHORIZED, "Unauthenticated!");
        }

        String email = auth.getName(); // tuỳ bạn mapping email/username, sửa nếu cần

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    @Override
    @Transactional
    public InventoryAuditResponse adjustStock(Long productId, AdjustStockRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Product not found!"));

        User admin = getCurrentUser();

        int oldStock = product.getStock() == null ? 0 : product.getStock();
        int delta = request.getQuantityChange();

        int newStock = oldStock + delta;

        if (newStock < 0) {
            throw new ApplicationException(
                    HttpStatus.BAD_REQUEST,
                    "Stock cannot be negative!"
            );
        }

        // cập nhật tồn kho: oldStock -> newStock = oldStock + delta
        product.setStock(newStock);
        productRepository.save(product);

        checkLowStockAndNotify(product, newStock);

        // tạo audit log
        InventoryAuditLog log = InventoryAuditLog.builder()
                .product(product)
                .changedBy(admin)
                .oldStock(oldStock)
                .newStock(newStock)
                .reason(request.getReason())
                .build();

        log = auditLogRepository.save(log);

        return inventoryMapper.toResponse(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryAuditResponse> getInventoryLogs(
            Long productId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "changedAt"));

        // Nếu không truyền from/to thì cho khoảng rất rộng
        LocalDateTime from = fromDate != null
                ? fromDate.atStartOfDay()
                : LocalDate.of(2000, 1, 1).atStartOfDay();

        LocalDateTime to = toDate != null
                ? toDate.atTime(LocalTime.MAX)
                : LocalDate.of(2100, 1, 1).atTime(LocalTime.MAX);

        Page<InventoryAuditLog> logsPage;

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with id=" + productId
                    ));
            logsPage = auditLogRepository
                    .findByProductAndChangedAtBetweenOrderByChangedAtDesc(product, from, to, pageable);
        } else {
            logsPage = auditLogRepository
                    .findByChangedAtBetweenOrderByChangedAtDesc(from, to, pageable);
        }

        List<InventoryAuditResponse> content = logsPage.getContent()
                .stream()
                .map(inventoryMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, logsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAuditLogsToExcel(Long productId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate != null
                ? fromDate.atStartOfDay()
                : LocalDate.of(2000, 1, 1).atStartOfDay();

        LocalDateTime to = toDate != null
                ? toDate.atTime(LocalTime.MAX)
                : LocalDate.of(2100, 1, 1).atTime(LocalTime.MAX);

        List<InventoryAuditLog> logs;

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with id=" + productId
                    ));
            logs = auditLogRepository
                    .findByProductAndChangedAtBetweenOrderByChangedAtAsc(product, from, to);
        } else {
            logs = auditLogRepository
                    .findByChangedAtBetweenOrderByChangedAtAsc(from, to);
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Inventory Audit");

            // Header
            Row header = sheet.createRow(0);
            int col = 0;
            header.createCell(col++).setCellValue("ID");
            header.createCell(col++).setCellValue("Product ID");
            header.createCell(col++).setCellValue("Product Name");
            header.createCell(col++).setCellValue("Old Stock");
            header.createCell(col++).setCellValue("New Stock");
            header.createCell(col++).setCellValue("Change");
            header.createCell(col++).setCellValue("Reason");
            header.createCell(col++).setCellValue("Changed By");
            header.createCell(col).setCellValue("Changed At");

            // Body
            int rowIdx = 1;
            for (InventoryAuditLog log : logs) {
                InventoryAuditResponse dto = inventoryMapper.toResponse(log);

                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                int oldStock = dto.getOldStock();
                int newStock = dto.getNewStock();
                int change = newStock - oldStock;

                row.createCell(c++).setCellValue(dto.getId());
                row.createCell(c++).setCellValue(dto.getProductId());
                row.createCell(c++).setCellValue(dto.getProductName());
                row.createCell(c++).setCellValue(oldStock);
                row.createCell(c++).setCellValue(newStock);
                row.createCell(c++).setCellValue(change);
                row.createCell(c++).setCellValue(
                        dto.getReason() != null ? dto.getReason() : ""
                );
                row.createCell(c++).setCellValue(
                        dto.getChangedByUserName() != null ? dto.getChangedByUserName() : ""
                );
                row.createCell(c).setCellValue(
                        dto.getChangedAt() != null ? dto.getChangedAt().toString() : ""
                );
            }

            for (int i = 0; i <= col; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to export Excel: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAuditLogsToPdf(Long productId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = fromDate != null
                ? fromDate.atStartOfDay()
                : LocalDate.of(2000, 1, 1).atStartOfDay();

        LocalDateTime to = toDate != null
                ? toDate.atTime(LocalTime.MAX)
                : LocalDate.of(2100, 1, 1).atTime(LocalTime.MAX);

        List<InventoryAuditLog> logs;

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ApplicationException(
                            HttpStatus.NOT_FOUND,
                            "Product not found with id=" + productId
                    ));
            logs = auditLogRepository
                    .findByProductAndChangedAtBetweenOrderByChangedAtAsc(product, from, to);
        } else {
            logs = auditLogRepository
                    .findByChangedAtBetweenOrderByChangedAtAsc(from, to);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 9);

            Paragraph title = new Paragraph("Inventory Audit Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            if (fromDate != null || toDate != null) {
                String rangeText = String.format("Date range: %s - %s",
                        fromDate != null ? fromDate.toString() : "ALL",
                        toDate != null ? toDate.toString() : "ALL"
                );
                Paragraph range = new Paragraph(rangeText, cellFont);
                range.setAlignment(Element.ALIGN_CENTER);
                range.setSpacingAfter(10f);
                document.add(range);
            }

            document.add(new Paragraph(" ")); // empty line

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 5f, 3f, 3f, 3f, 5f, 4f, 5f});

            // Header
            addHeaderCell(table, "ID", headerFont);
            addHeaderCell(table, "Product ID", headerFont);
            addHeaderCell(table, "Product Name", headerFont);
            addHeaderCell(table, "Old Stock", headerFont);
            addHeaderCell(table, "New Stock", headerFont);
            addHeaderCell(table, "Change", headerFont);
            addHeaderCell(table, "Reason", headerFont);
            addHeaderCell(table, "Changed By", headerFont);
            addHeaderCell(table, "Changed At", headerFont);

            // Body
            for (InventoryAuditLog log : logs) {
                InventoryAuditResponse dto = inventoryMapper.toResponse(log);

                int oldStock = dto.getOldStock();
                int newStock = dto.getNewStock();
                int change = newStock - oldStock;

                addBodyCell(table, dto.getId() != null ? dto.getId().toString() : "", cellFont);
                addBodyCell(table, dto.getProductId() != null ? dto.getProductId().toString() : "", cellFont);
                addBodyCell(table, dto.getProductName(), cellFont);
                addBodyCell(table, String.valueOf(oldStock), cellFont);
                addBodyCell(table, String.valueOf(newStock), cellFont);
                addBodyCell(table, String.valueOf(change), cellFont);
                addBodyCell(table, dto.getReason(), cellFont);
                addBodyCell(table, dto.getChangedByUserName(), cellFont);
                addBodyCell(table, dto.getChangedAt() != null ? dto.getChangedAt().toString() : "", cellFont);
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to export PDF: " + e.getMessage());
        }
    }

    private void checkLowStockAndNotify(Product product, int currentStock) {
        if (currentStock >= lowStockThreshold) {
            return;
        }

        var inventoryUsers = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("INVENTORY");
        var adminUsers = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("ADMIN");

        var emails = new java.util.HashSet<String>();
        inventoryUsers.forEach(u -> emails.add(u.getEmail()));
        adminUsers.forEach(u -> emails.add(u.getEmail()));

        if (emails.isEmpty()) {
            return;
        }

        notificationService.sendEmail(
                NotificationType.LOW_STOCK_ALERT,
                emails,
                java.util.Map.of(
                        "productName", product.getName(),
                        "stock", currentStock
                )
        );
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }
}
