package com.zyna.dev.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustStockRequest {

    /**
     * Số lượng thay đổi:
     *  - > 0: nhập thêm hàng (increase)
     *  - < 0: xuất/bớt hàng (decrease)
     */
    @NotNull
    private Integer quantityChange;

    // lý do điều chỉnh: nhập hàng, kiểm kê, hàng hư, ...
    private String reason;
}
