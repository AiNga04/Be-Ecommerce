package com.zyna.dev.ecommerce.vouchers.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherCreateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.request.VoucherUpdateRequest;
import com.zyna.dev.ecommerce.vouchers.dto.response.VoucherResponse;
import com.zyna.dev.ecommerce.vouchers.service.interfaces.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // ADMIN: tạo voucher (DRAFT) -> chỉ ADMIN có VOUCHER_WRITE
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VOUCHER_WRITE')")
    public ApiResponse<VoucherResponse> create(@Valid @RequestBody VoucherCreateRequest request) {
        VoucherResponse data = voucherService.create(request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Voucher created successfully!",
                data
        );
    }

    // ADMIN: update nội dung voucher -> chỉ ADMIN
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('VOUCHER_WRITE')")
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

    // ADMIN: deactivate / xóa logic (INACTIVE) -> chỉ ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('VOUCHER_WRITE')")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        voucherService.deactivate(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Voucher deactivated successfully!"
        );
    }

    // ADMIN + STAFF: activate -> ACTIVE
    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('VOUCHER_STATUS_MANAGE')") // admin + staff
    public ApiResponse<Void> activate(@PathVariable Long id) {
        voucherService.activate(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Voucher activated successfully!"
        );
    }

    // ADMIN + STAFF: xem chi tiết 1 voucher
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('VOUCHER_READ')")
    public ApiResponse<VoucherResponse> getById(@PathVariable Long id) {
        VoucherResponse data = voucherService.getById(id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get voucher successfully!",
                data
        );
    }

    // ADMIN + STAFF: xem full list
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('VOUCHER_READ')")
    public ApiResponse<List<VoucherResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<VoucherResponse> data = voucherService.list(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get voucher list successfully!",
                data
        );
    }

    // USER: xem list voucher đang áp dụng (ACTIVE & trong thời gian hiệu lực)
    @GetMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')") // chỉ user (shopper) có
    public ApiResponse<List<VoucherResponse>> listActiveForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<VoucherResponse> data = voucherService.listActive(page, size);
        return ApiResponse.successfulPageResponse(
                HttpStatus.OK.value(),
                "Get active voucher list successfully!",
                data
        );
    }

}
