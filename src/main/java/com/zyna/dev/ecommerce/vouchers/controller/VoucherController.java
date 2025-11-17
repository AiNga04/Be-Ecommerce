package com.zyna.dev.ecommerce.vouchers.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherApplyRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherApplyResponse;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import com.zyna.dev.ecommerce.vouchers.service.interfaces.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ADMIN: tạo voucher
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')") // hoặc tạo riêng 'VOUCHER_MANAGE'
    public ApiResponse<VoucherResponse> create(@Valid @RequestBody VoucherCreateRequest request) {
        VoucherResponse data = voucherService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Voucher created successfully!",
                data
        );
    }

    // ADMIN: update
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<VoucherResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VoucherUpdateRequest request
    ) {
        VoucherResponse data = voucherService.update(id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Voucher updated successfully!",
                data
        );
    }

    // ADMIN: deactivate (soft)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        voucherService.deactivate(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Voucher deactivated successfully!"
        );
    }

    // ADMIN: get by id
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<VoucherResponse> getById(@PathVariable Long id) {
        VoucherResponse data = voucherService.getById(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get voucher successfully!",
                data
        );
    }

    // ADMIN: list
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Page<VoucherResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<VoucherResponse> data = voucherService.list(page, size);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get voucher list successfully!",
                data
        );
    }

    // USER / PUBLIC: apply voucher cho đơn hàng
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherApplyResponse> apply(
            @Valid @RequestBody VoucherApplyRequest request
    ) {
        VoucherApplyResponse data = voucherService.apply(request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Apply voucher result",
                data
        );
    }
}
