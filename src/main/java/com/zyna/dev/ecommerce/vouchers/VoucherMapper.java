package com.zyna.dev.ecommerce.vouchers;

import com.zyna.dev.ecommerce.common.enums.VoucherStatus;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import com.zyna.dev.ecommerce.vouchers.models.Voucher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VoucherMapper {

    public Voucher toEntity(VoucherCreateRequest dto) {
        return Voucher.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .type(dto.getType())
                .scope(dto.getScope())
                .status(VoucherStatus.DRAFT)         // ⭐ tạo ra ở trạng thái DRAFT
                .discountValue(dto.getDiscountValue())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .minOrderValue(dto.getMinOrderValue())
                .maxUsage(dto.getMaxUsage())
                .maxUsagePerUser(dto.getMaxUsagePerUser())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usedCount(0)
                .build();
    }

    public void applyUpdate(Voucher voucher, VoucherUpdateRequest dto) {
        if (dto.getName() != null) voucher.setName(dto.getName().trim());
        if (dto.getDescription() != null) voucher.setDescription(dto.getDescription());
        if (dto.getType() != null) voucher.setType(dto.getType());
        if (dto.getScope() != null) voucher.setScope(dto.getScope());
        if (dto.getDiscountValue() != null) voucher.setDiscountValue(dto.getDiscountValue());
        if (dto.getMaxDiscountAmount() != null) voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        if (dto.getMinOrderValue() != null) voucher.setMinOrderValue(dto.getMinOrderValue());
        if (dto.getMaxUsagePerUser() != null) voucher.setMaxUsagePerUser(dto.getMaxUsagePerUser());
        if (dto.getMaxUsage() != null) voucher.setMaxUsage(dto.getMaxUsage());
        if (dto.getStartDate() != null) voucher.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) voucher.setEndDate(dto.getEndDate());
        // Không cho update status ở đây, mình tách thành API riêng (activate/deactivate)
        voucher.setUpdatedAt(LocalDateTime.now());
    }

    public VoucherResponse toResponse(Voucher entity) {
        return VoucherResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType())
                .scope(entity.getScope())
                .status(entity.getStatus()) // ⭐ trả cả status
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minOrderValue(entity.getMinOrderValue())
                .maxUsagePerUser(entity.getMaxUsagePerUser())
                .maxUsage(entity.getMaxUsage())
                .usedCount(entity.getUsedCount())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
