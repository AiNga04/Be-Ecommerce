package com.zyna.dev.ecommerce.address.service.impl;

import com.zyna.dev.ecommerce.address.dto.request.ShippingAddressRequest;
import com.zyna.dev.ecommerce.address.dto.response.ShippingAddressResponse;
import com.zyna.dev.ecommerce.address.ShippingAddressMapper;
import com.zyna.dev.ecommerce.address.models.ShippingAddress;
import com.zyna.dev.ecommerce.address.repository.ShippingAddressRepository;
import com.zyna.dev.ecommerce.address.service.interfaces.ShippingAddressService;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAddressServiceImpl implements ShippingAddressService {

    private final ShippingAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ShippingAddressMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ShippingAddressResponse> getMyAddresses(Long userId) {
        User user = getUser(userId);
        List<ShippingAddress> list = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return mapper.toResponses(list);
    }

    @Override
    @Transactional
    public ShippingAddressResponse createAddress(Long userId, ShippingAddressRequest request) {
        User user = getUser(userId);

        ShippingAddress address = ShippingAddress.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .fullAddress(request.getFullAddress())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detailAddress(request.getDetailAddress())
                .isDefault(false)
                .build();

        // nếu user chưa có default hoặc request setAsDefault = true => set default
        boolean setDefault = Boolean.TRUE.equals(request.getSetAsDefault());
        if (setDefault || addressRepository.findByUserAndIsDefaultTrue(user).isEmpty()) {
            // bỏ default hiện tại
            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(a -> {
                a.setDefault(false);
                addressRepository.save(a);
            });
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        return mapper.toResponse(address);
    }

    @Override
    @Transactional
    public ShippingAddressResponse updateAddress(Long userId, Long addressId, ShippingAddressRequest request) {
        User user = getUser(userId);

        ShippingAddress address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Address not found!"));

        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setFullAddress(request.getFullAddress());
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setDetailAddress(request.getDetailAddress());

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            // cần 1 biến effectively final để dùng trong lambda
            Long currentAddressId = address.getId();

            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(a -> {
                if (!a.getId().equals(currentAddressId)) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            });
            address.setDefault(true);
        }

        address = addressRepository.save(address);
        return mapper.toResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        User user = getUser(userId);
        ShippingAddress address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Address not found"));

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public ShippingAddressResponse setDefaultAddress(Long userId, Long addressId) {
        User user = getUser(userId);
        ShippingAddress address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Address not found"));

        addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(a -> {
            if (!a.getId().equals(addressId)) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        });

        address.setDefault(true);
        address = addressRepository.save(address);
        return mapper.toResponse(address);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
