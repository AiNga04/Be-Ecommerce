package com.zyna.dev.ecommerce.vouchers.service.impl;

import com.zyna.dev.ecommerce.common.enums.VoucherType;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.vouchers.VoucherMapper;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherApplyRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherApplyResponse;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import com.zyna.dev.ecommerce.vouchers.models.Voucher;
import com.zyna.dev.ecommerce.vouchers.repository.VoucherRepository;
import com.zyna.dev.ecommerce.vouchers.service.interfaces.VoucherService;
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

    @Override
    public VoucherResponse create(VoucherCreateRequest request) {
        if (voucherRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new ApplicationException(HttpStatus.CONFLICT, "Voucher code already exists!");
        }

        Voucher voucher = voucherMapper.toEntity(request);
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponse(saved);
    }

    @Override
    public VoucherResponse update(Long id, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Voucher not found!"));

        voucherMapper.applyUpdate(voucher, request);
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponse(saved);
    }

    @Override
    public void deactivate(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Voucher not found!"));

        voucher.setIsActive(false);
        voucherRepository.save(voucher);
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
    public VoucherApplyResponse apply(VoucherApplyRequest request) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndIsActiveTrue(request.getCode())
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Voucher not found or inactive!"));

        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            return invalid("Voucher is not started yet!", request);
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            return invalid("Voucher is expired!", request);
        }

        BigDecimal cartTotal = request.getCartTotal();
        BigDecimal shippingFee = request.getShippingFee();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal shippingDiscount = BigDecimal.ZERO;

        // check minOrderValue
        if (voucher.getMinOrderValue() != null &&
                cartTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return invalid("Order total does not reach minimum value for this voucher!", request);
        }

        if (voucher.getType() == VoucherType.PERCENTAGE) {
            BigDecimal percent = voucher.getDiscountValue();
            discountAmount = cartTotal
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100));

            if (voucher.getMaxDiscountAmount() != null &&
                    discountAmount.compareTo(voucher.getMaxDiscountAmount()) > 0) {
                discountAmount = voucher.getMaxDiscountAmount();
            }
        } else if (voucher.getType() == VoucherType.FIXED_AMOUNT) {
            discountAmount = voucher.getDiscountValue();
            if (discountAmount.compareTo(cartTotal) > 0) {
                discountAmount = cartTotal;
            }
        } else if (voucher.getType() == VoucherType.FREESHIP) {
            // nếu discountValue null, coi như freeship toàn bộ
            BigDecimal maxShipDiscount = voucher.getDiscountValue() != null
                    ? voucher.getDiscountValue()
                    : shippingFee;
            shippingDiscount = shippingFee.min(maxShipDiscount);
        }

        BigDecimal finalCartTotal = cartTotal.subtract(discountAmount);
        BigDecimal finalShipping = shippingFee.subtract(shippingDiscount);
        BigDecimal finalPayable = finalCartTotal.add(finalShipping);

        return VoucherApplyResponse.builder()
                .valid(true)
                .message("Voucher applied successfully!")
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
}
