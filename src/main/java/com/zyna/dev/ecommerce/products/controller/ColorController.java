package com.zyna.dev.ecommerce.products.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.products.dto.request.ColorRequest;
import com.zyna.dev.ecommerce.products.models.Color;
import com.zyna.dev.ecommerce.products.service.ColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public ApiResponse<List<Color>> getAllColors() {
        return ApiResponse.successfulResponse(
                "Fetched all colors!",
                colorService.getAllColors()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<Color> getColorById(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Fetched color successfully!",
                colorService.getColorById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Color> createColor(@RequestBody ColorRequest request) {
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Color created successfully!",
                colorService.createColor(request)
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Color> updateColor(@PathVariable Long id, @RequestBody ColorRequest request) {
        return ApiResponse.successfulResponse(
                "Color updated successfully!",
                colorService.updateColor(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> deleteColor(@PathVariable Long id) {
        colorService.deleteColor(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Color deleted successfully!"
        );
    }
}
