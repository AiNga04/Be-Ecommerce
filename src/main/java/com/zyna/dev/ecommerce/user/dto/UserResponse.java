package com.zyna.dev.ecommerce.user.dto;

import com.zyna.dev.ecommerce.common.enums.City;
import com.zyna.dev.ecommerce.common.enums.Gender;
import com.zyna.dev.ecommerce.common.enums.Role;
import com.zyna.dev.ecommerce.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime dateOfBirth;
    private Gender gender;
    private Role role;
    private String phone;
    private String address;
    private City city;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
