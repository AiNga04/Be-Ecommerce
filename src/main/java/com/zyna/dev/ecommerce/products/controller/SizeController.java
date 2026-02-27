package com.zyna.dev.ecommerce.products.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.products.dto.request.SizeRequest;
import com.zyna.dev.ecommerce.products.models.Size;
import com.zyna.dev.ecommerce.products.service.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @GetMapping
    public ApiResponse<List<Size>> getAllSizes() {
        return ApiResponse.successfulResponse(
                "Lấy danh sách kích thước thành công",
                sizeService.getAllSizes()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<Size> getSizeById(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Lấy thông tin kích thước thành công",
                sizeService.getSizeById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Size> createSize(@RequestBody SizeRequest request) {
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Tạo kích thước thành công",
                sizeService.createSize(request)
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Size> updateSize(@PathVariable Long id, @RequestBody SizeRequest request) {
        return ApiResponse.successfulResponse(
                "Cập nhật kích thước thành công",
                sizeService.updateSize(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> deleteSize(@PathVariable Long id) {
        sizeService.deleteSize(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Xóa kích thước thành công"
        );
    }
}
