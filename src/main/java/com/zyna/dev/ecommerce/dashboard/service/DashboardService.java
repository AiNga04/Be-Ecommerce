package com.zyna.dev.ecommerce.dashboard.service;

import com.zyna.dev.ecommerce.dashboard.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {
    RevenueDashboardResponse getRevenueStats(LocalDate from, LocalDate to);

    OrderStatResponse getOrderStats();

    List<TopProductResponse> getTopSellingProducts(int limit);

    List<LowStockResponse> getLowStockProducts(int threshold);

    UserStatResponse getUserStats();
}
