package com.zyna.dev.ecommerce.shipping.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignShipmentRequest {
    private Long shipperId;
    private String carrierCode;
}
