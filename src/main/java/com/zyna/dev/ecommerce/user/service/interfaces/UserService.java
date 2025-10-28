package com.zyna.dev.ecommerce.user.service.interfaces;

import com.zyna.dev.ecommerce.user.dto.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreateRequest createRequest);
}
