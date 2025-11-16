package com.zyna.dev.ecommerce.address;

import com.zyna.dev.ecommerce.address.dto.request.ShippingAddressRequest;
import com.zyna.dev.ecommerce.address.dto.response.ShippingAddressResponse;
import com.zyna.dev.ecommerce.address.service.interfaces.ShippingAddressService;
import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class ShippingAddressController {

    private final ShippingAddressService addressService;
    private final UserRepository userRepository;

    private Long getCurrentUserId(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return userRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<List<ShippingAddressResponse>> getMyAddresses(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<ShippingAddressResponse> result = addressService.getMyAddresses(userId);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Get addresses successfully",
                result
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ShippingAddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody ShippingAddressRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        ShippingAddressResponse res = addressService.createAddress(userId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.CREATED.value(),
                "Create address successfully",
                res
        );
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ShippingAddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ShippingAddressRequest request
    ) {
        Long userId = getCurrentUserId(authentication);
        ShippingAddressResponse res = addressService.updateAddress(userId, id, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Update address successfully",
                res
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        addressService.deleteAddress(userId, id);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Address deleted successfully!"
        );
    }

    @PatchMapping("/{id}/default")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('ORDER_WRITE')")
    public ApiResponse<ShippingAddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long userId = getCurrentUserId(authentication);
        ShippingAddressResponse res = addressService.setDefaultAddress(userId, id);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Set default address successfully",
                res
        );
    }
}
