package com.zyna.dev.ecommerce.orders.dto.response;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String size;
    private String image;
}
