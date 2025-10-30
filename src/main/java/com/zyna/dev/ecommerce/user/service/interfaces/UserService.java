package com.zyna.dev.ecommerce.user.service.interfaces;

import com.zyna.dev.ecommerce.user.criteria.UserCriteria;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import org.springframework.data.domain.Page;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    Page<UserResponse> searchUsers(UserCriteria criteria, int page, int size);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void softDeleteUser(Long id);

    void restoreUser(Long id);

    void hardDeleteUser(Long id);
}
