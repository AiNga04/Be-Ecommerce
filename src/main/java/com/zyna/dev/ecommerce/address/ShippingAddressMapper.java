package com.zyna.dev.ecommerce.address;

import com.zyna.dev.ecommerce.address.dto.response.ShippingAddressResponse;
import com.zyna.dev.ecommerce.address.models.ShippingAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShippingAddressMapper {

    public ShippingAddressResponse toResponse(ShippingAddress entity) {
        return ShippingAddressResponse.builder()
                .id(entity.getId())
                .receiverName(entity.getReceiverName())
                .receiverPhone(entity.getReceiverPhone())
                .fullAddress(entity.getFullAddress())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .ward(entity.getWard())
                .detailAddress(entity.getDetailAddress())
                .isDefault(entity.isDefault())
                .build();
    }

    public List<ShippingAddressResponse> toResponses(List<ShippingAddress> list) {
        return list.stream().map(this::toResponse).toList();
    }
}
