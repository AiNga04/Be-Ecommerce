package com.zyna.dev.ecommerce.user.service.interfaces;

import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;

public interface UserService {
    UserResponse createUser(UserCreateRequest createRequest);
    UserResponse updateUser(Long id, UserUpdateRequest updateRequest);
}
