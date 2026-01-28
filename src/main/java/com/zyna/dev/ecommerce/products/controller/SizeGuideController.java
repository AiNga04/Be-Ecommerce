package com.zyna.dev.ecommerce.products.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.products.models.SizeGuide;
import com.zyna.dev.ecommerce.products.service.SizeGuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/size-guides")
@RequiredArgsConstructor
public class SizeGuideController {

    private final SizeGuideService sizeGuideService;

    @GetMapping
    public ApiResponse<List<SizeGuide>> getAllSizeGuides() {
        return ApiResponse.successfulResponse(
                "Fetched all size guides!",
                sizeGuideService.getAllSizeGuides()
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<SizeGuide> getSizeGuideById(@PathVariable Long id) {
        return ApiResponse.successfulResponse(
                "Fetched size guide successfully!",
                sizeGuideService.getSizeGuideById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<SizeGuide> createSizeGuide(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image
    ) {
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Size guide created successfully!",
                sizeGuideService.createSizeGuide(name, description, image)
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<SizeGuide> updateSizeGuide(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile image
    ) {
        return ApiResponse.successfulResponse(
                "Size guide updated successfully!",
                sizeGuideService.updateSizeGuide(id, name, description, image)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public ApiResponse<Void> deleteSizeGuide(@PathVariable Long id) {
        sizeGuideService.deleteSizeGuide(id);
        return ApiResponse.successfulResponseNoData(
                HttpStatus.OK.value(),
                "Size guide deleted successfully!"
        );
    }
}
