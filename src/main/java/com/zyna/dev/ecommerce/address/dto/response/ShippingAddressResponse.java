package com.zyna.dev.ecommerce.address.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShippingAddressResponse {

    private Long id;

    private String receiverName;
    private String receiverPhone;
    private String fullAddress;

    private String province;
    private String district;
    private String ward;
    private String detailAddress;

    private boolean isDefault;
}
