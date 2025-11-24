package com.zyna.dev.ecommerce.users;

import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import com.zyna.dev.ecommerce.users.dto.request.UserCreateRequest;
import com.zyna.dev.ecommerce.users.dto.request.UserUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User createToUser(UserCreateRequest dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .avatarUrl(null)
                .build();
    }


    public void applyUpdate(User target, UserUpdateRequest dto) {
        if (dto.getFirstName() != null) target.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) target.setLastName(dto.getLastName());
        if (dto.getDateOfBirth() != null) target.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getGender() != null) target.setGender(dto.getGender());
        if (dto.getPhone() != null) target.setPhone(dto.getPhone());
        if (dto.getAddress() != null) target.setAddress(dto.getAddress());
        if (dto.getCity() != null) target.setCity(dto.getCity());
        if (dto.getAvatarUrl() != null) target.setAvatarUrl(dto.getAvatarUrl());
    }

    public UserResponse toUserResponse(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .roles(
                        entity.getRoles() == null ? null :
                                entity.getRoles().stream()
                                        .map(r -> r.getCode())
                                        .collect(Collectors.toSet())
                )
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .city(entity.getCity())
                .avatarUrl(entity.getAvatarUrl())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
