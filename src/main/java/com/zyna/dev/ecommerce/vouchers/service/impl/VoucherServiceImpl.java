package com.zyna.dev.ecommerce.vouchers.service.impl;

import com.zyna.dev.ecommerce.common.enums.VoucherStatus;
import com.zyna.dev.ecommerce.common.enums.VoucherType;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.notifications.NotificationService;
import com.zyna.dev.ecommerce.notifications.NotificationType;
import com.zyna.dev.ecommerce.vouchers.VoucherMapper;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherApplyRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherApplyResponse;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import com.zyna.dev.ecommerce.vouchers.models.Voucher;
import com.zyna.dev.ecommerce.vouchers.repository.VoucherRepository;
import com.zyna.dev.ecommerce.vouchers.service.interfaces.VoucherService;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    public VoucherResponse create(VoucherCreateRequest request) {
        if (voucherRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Mã voucher đã tồn tại!");
        }

        validateDates(request.getStartDate(), request.getEndDate());
        validateDiscountConfig(request.getType(), request.getDiscountValue());

        Voucher voucher = voucherMapper.toEntity(request); // status = DRAFT trong mapper
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponse(saved);
    }

    @Override
    public VoucherResponse update(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher!"));

        VoucherType typeAfterUpdate = request.getType() != null ? request.getType() : voucher.getType();
        BigDecimal discountAfterUpdate = request.getDiscountValue() != null
                ? request.getDiscountValue()
                : voucher.getDiscountValue();

        LocalDateTime startDateAfterUpdate = request.getStartDate() != null
                ? request.getStartDate()
                : voucher.getStartDate();
        LocalDateTime endDateAfterUpdate = request.getEndDate() != null
                ? request.getEndDate()
                : voucher.getEndDate();

        validateDates(startDateAfterUpdate, endDateAfterUpdate);
        validateDiscountConfig(typeAfterUpdate, discountAfterUpdate);

        // không cho sửa voucher đã hết hạn
        if (voucher.getStatus() == VoucherStatus.EXPIRED) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Voucher đã hết hạn không thể cập nhật!");
        }

        voucherMapper.applyUpdate(voucher, request);
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponse(saved);
    }

    /**
     * Admin tắt voucher (INACTIVE)
     */
    @Override
    public void deactivate(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher!"));

        voucher.setStatus(VoucherStatus.INACTIVE);
        voucherRepository.save(voucher);
    }

    /**
     * Admin bật voucher (ACTIVE) từ DRAFT/INACTIVE
     */
    @Override
    public void activate(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher!"));

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucherRepository.save(voucher);
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Voucher đã hết hạn!");
        }

        voucher.setStatus(VoucherStatus.ACTIVE);
        voucherRepository.save(voucher);

        var admins = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("ADMIN")
                .stream().map(u -> u.getEmail()).toList();
        notificationService.sendEmail(
                NotificationType.VOUCHER_ACTIVATED,
                admins,
                java.util.Map.of(
                        "code", voucher.getCode(),
                        "endDate", voucher.getEndDate()
                )
        );
    }

    @Override
    public VoucherResponse getById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Voucher not found!"));
        return voucherMapper.toResponse(voucher);
    }

    @Override
    public Page<VoucherResponse> list(int page, int size) {
        Page<Voucher> vouchers = voucherRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return vouchers.map(voucherMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> listActive(int page, int size) {
        // Lấy các voucher ACTIVE, rồi filter thêm theo startDate/endDate
        Page<Voucher> vouchers = voucherRepository.findAllByStatus(
                VoucherStatus.ACTIVE,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        LocalDateTime now = LocalDateTime.now();

        var filtered = vouchers.getContent().stream()
                .filter(v -> (v.getStartDate() == null || !now.isBefore(v.getStartDate()))
                        && (v.getEndDate() == null || !now.isAfter(v.getEndDate())))
                .map(voucherMapper::toResponse)
                .toList();

        return new PageImpl<>(filtered, vouchers.getPageable(), filtered.size());
    }


    /**
     * User apply voucher khi checkout
     */
    @Override
    @Transactional
    public VoucherApplyResponse apply(VoucherApplyRequest request) {
        String code = request.getCode().trim();
        Voucher voucher = voucherRepository
                .findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ApplicationException(
                        HttpStatus.NOT_FOUND,
                        "Voucher not found!"
                ));

        LocalDateTime now = LocalDateTime.now();

        // cập nhật trạng thái hết hạn nếu đã quá endDate
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            voucher.setStatus(VoucherStatus.EXPIRED);
            voucherRepository.save(voucher);
            return invalid("Voucher đã hết hạn!", request);
        }

        if (voucher.getStatus() == VoucherStatus.DRAFT || voucher.getStatus() == VoucherStatus.INACTIVE) {
            return invalid("Voucher đang bị khóa hoặc ở trạng thái nháp!", request);
        }

        if (voucher.getStatus() == VoucherStatus.EXPIRED) {
            return invalid("Voucher is expired!", request);
        }

        // thời gian hiệu lực
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return invalid("Voucher chưa đến thời gian áp dụng!", request);
        }

        try {
            validateDiscountConfig(voucher.getType(), voucher.getDiscountValue());
        } catch (ApplicationException ex) {
            return invalid(ex.getMessage(), request);
        }

        BigDecimal cartTotal = request.getCartTotal();
        BigDecimal shippingFee = request.getShippingFee();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;

        // đơn tối thiểu
        if (voucher.getMinOrderValue() != null &&
                cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return invalid("Giá trị đơn hàng chưa đạt mức tối thiểu để áp dụng mã này!", request);
        }

        // (optional) giới hạn số lần dùng tổng
        int currentUsed = voucher.getUsedCount() != null ? voucher.getUsedCount() : 0;
        if (voucher.getMaxUsage() != null && currentUsed >= voucher.getMaxUsage()) {
            voucher.setStatus(VoucherStatus.INACTIVE);
            voucherRepository.save(voucher);
            return invalid("Voucher này đã đạt số lần sử dụng tối đa!", request);
        }

        // TÍNH GIẢM GIÁ
        if (voucher.getType() == VoucherType.PERCENTAGE) {

            BigDecimal percent = voucher.getDiscountValue();
            if (percent == null || percent.compareTo(BigDecimal.ZERO) <= 0) {
                return invalid("Giảm giá của voucher chưa được cấu hình đúng!", request);
            }
            discountAmount = cartTotal
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100));

            if (voucher.getMaxDiscountAmount() != null &&
                    discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
            if (discountAmount.compareTo(cartTotal) > 0) {
                discountAmount = cartTotal;
            }

        } else if (voucher.getType() == VoucherType.FIXED_AMOUNT) {

            discountAmount = voucher.getDiscountValue();
            if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return invalid("Voucher discount is not configured correctly!", request);
            }
            if (discountAmount.compareTo(cartTotal) > 0) {
                discountAmount = cartTotal;
            }

        } else if (voucher.getType() == VoucherType.FREESHIP) {

            BigDecimal maxShipDiscount = voucher.getDiscountValue() != null
                    ? voucher.getDiscountValue()
                    : shippingFee;
            shippingDiscount = shippingFee.min(maxShipDiscount);
        }

        BigDecimal finalCartTotal = cartTotal.subtract(discountAmount);
        if (finalCartTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalCartTotal = BigDecimal.ZERO;
        }
        BigDecimal finalShipping = shippingFee.subtract(shippingDiscount);
        if (finalShipping.compareTo(BigDecimal.ZERO) < 0) {
            finalShipping = BigDecimal.ZERO;
        }
        BigDecimal finalPayable = finalCartTotal.add(finalShipping);

        // tăng usedCount
        int newUsedCount = currentUsed + 1;
        voucher.setUsedCount(newUsedCount);
        if (voucher.getMaxUsage() != null && newUsedCount >= voucher.getMaxUsage()) {
            voucher.setStatus(VoucherStatus.INACTIVE);
        }
        voucherRepository.save(voucher);

        return VoucherApplyResponse.builder()
                .valid(true)
                .message("Áp dụng voucher thành công!")
                .cartTotal(cartTotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .shippingDiscount(shippingDiscount)
                .finalPayable(finalPayable)
                .build();
    }

    private VoucherApplyResponse invalid(String message, VoucherApplyRequest request) {
        BigDecimal cartTotal = request.getCartTotal();
        BigDecimal shippingFee = request.getShippingFee();
        return VoucherApplyResponse.builder()
                .valid(false)
                .message(message)
                .cartTotal(cartTotal)
                .shippingFee(shippingFee)
                .discountAmount(BigDecimal.ZERO)
                .shippingDiscount(BigDecimal.ZERO)
                .finalPayable(cartTotal.add(shippingFee))
                .build();
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private void validateDiscountConfig(VoucherType type, BigDecimal discountValue) {
        if (type == null) return;

        if (type == VoucherType.FREESHIP) {
            if (discountValue != null && discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ApplicationException(HttpStatus.BAD_REQUEST, "Giá trị giảm phí vận chuyển phải lớn hơn 0");
            }
            return;
        }

        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Giá trị giảm giá phải lớn hơn 0");
        }

        if (type == VoucherType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Giá trị giảm giá theo phần trăm không được quá 100");
        }
    }
}
