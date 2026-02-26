package com.zyna.dev.ecommerce.dashboard.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.dashboard.dto.*;
import com.zyna.dev.ecommerce.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // REVENUE
    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('DASHBOARD_READ') or hasRole('ADMIN')")
    public ApiResponse<RevenueDashboardResponse> getRevenueStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();

        return ApiResponse.successfulResponse(
                "Fetched revenue stats!",
                dashboardService.getRevenueStats(from, to)
        );
    }

    // ORDERS
    @GetMapping("/orders/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_READ') or hasRole('ADMIN')")
    public ApiResponse<OrderStatResponse> getOrderStats() {
        return ApiResponse.successfulResponse(
                "Fetched order stats!",
                dashboardService.getOrderStats()
        );
    }

    // TOP PRODUCTS
    @GetMapping("/top-products")
    @PreAuthorize("hasAuthority('DASHBOARD_READ') or hasRole('ADMIN')")
    public ApiResponse<List<TopProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.successfulResponse(
                "Fetched top selling products!",
                dashboardService.getTopSellingProducts(limit)
        );
    }

    // LOW STOCK
    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('DASHBOARD_READ') or hasRole('ADMIN')")
    public ApiResponse<List<LowStockResponse>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold
    ) {
        return ApiResponse.successfulResponse(
                "Fetched low stock products!",
                dashboardService.getLowStockProducts(threshold)
        );
    }

    // USER STATS
    @GetMapping("/users/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_READ') or hasRole('ADMIN')")
    public ApiResponse<UserStatResponse> getUserStats() {
        return ApiResponse.successfulResponse(
                "Fetched user stats!",
                dashboardService.getUserStats()
        );
    }
}
