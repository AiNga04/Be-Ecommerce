package com.zyna.dev.ecommerce.user;

import com.zyna.dev.ecommerce.user.dto.response.UserResponse;
import com.zyna.dev.ecommerce.user.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.user.dto.request.UserUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User createToUser(UserCreateRequest dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .password(dto.getPassword())
                .role(dto.getRole())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .build();
    }

    public void applyUpdate(User target, UserUpdateRequest dto) {
        if (dto.getFirstName() != null) {
            target.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            target.setLastName(dto.getLastName());
        }
        if (dto.getDateOfBirth() != null) {
            target.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            target.setGender(dto.getGender());
        }
        if (dto.getPassword() != null) {
            target.setPassword(dto.getPassword());
        }
        if (dto.getRole() != null) {
            target.setRole(dto.getRole());
        }
        if (dto.getPhone() != null) {
            target.setPhone(dto.getPhone());
        }
        if (dto.getAddress() != null) {
            target.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            target.setCity(dto.getCity());
        }
    }

    public UserResponse toUserResponse(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .role(entity.getRole())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .city(entity.getCity())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
