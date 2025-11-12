package com.zyna.dev.ecommerce.authentication;

import com.zyna.dev.ecommerce.authentication.dto.request.RegisterRequest;
import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AuthMapper {

    public User toUser(RegisterRequest dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .build();
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
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
