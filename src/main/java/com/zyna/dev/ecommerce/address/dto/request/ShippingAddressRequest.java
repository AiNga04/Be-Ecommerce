package com.zyna.dev.ecommerce.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingAddressRequest {

    @NotBlank
    private String receiverName;

    @NotBlank
    private String receiverPhone;

    @NotBlank
    private String fullAddress;

    private String province;
    private String district;
    private String ward;
    private String detailAddress;

    private Boolean setAsDefault;
}
