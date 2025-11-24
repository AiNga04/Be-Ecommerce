package com.zyna.dev.ecommerce.users.service.interfaces;

import com.zyna.dev.ecommerce.users.criteria.UserCriteria;
import com.zyna.dev.ecommerce.users.dto.request.UserBatchCreateRequest;
import com.zyna.dev.ecommerce.users.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.users.dto.response.UserBatchCreateResponse;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import com.zyna.dev.ecommerce.users.dto.request.UserUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    Long getUserIdByEmail(String email);

    Page<UserResponse> searchUsers(UserCriteria criteria, int page, int size);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void softDeleteUser(Long id);

    void restoreUser(Long id);

    void hardDeleteUser(Long id);

    List<Long> softDeleteUsers(List<Long> ids);

    List<Long> restoreUsers(List<Long> ids);

    List<Long> hardDeleteUsers(List<Long> ids);

    Page<UserResponse> getDeletedUsers(UserCriteria criteria, int page, int size);

    UserBatchCreateResponse createUsers(UserBatchCreateRequest request);

    Page<UserResponse> getShippers(int page, int size);

    UserResponse updateAvatar(Long userId, org.springframework.web.multipart.MultipartFile image);

    UserResponse updateAvatarByAdmin(Long targetUserId, org.springframework.web.multipart.MultipartFile image);
}
