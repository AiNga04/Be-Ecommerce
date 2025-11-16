package com.zyna.dev.ecommerce.address.service.interfaces
;

import com.zyna.dev.ecommerce.address.dto.request.ShippingAddressRequest;
import com.zyna.dev.ecommerce.address.dto.response.ShippingAddressResponse;

import java.util.List;

public interface ShippingAddressService {

    List<ShippingAddressResponse> getMyAddresses(Long userId);

    ShippingAddressResponse createAddress(Long userId, ShippingAddressRequest request);

    ShippingAddressResponse updateAddress(Long userId, Long addressId, ShippingAddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    ShippingAddressResponse setDefaultAddress(Long userId, Long addressId);
}
