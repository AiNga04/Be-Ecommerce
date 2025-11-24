package com.zyna.dev.ecommerce.users.dto.response;

import com.zyna.dev.ecommerce.common.enums.City;
import com.zyna.dev.ecommerce.common.enums.Gender;
import com.zyna.dev.ecommerce.common.enums.Role;
import com.zyna.dev.ecommerce.common.enums.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Set<String> roles;
    private String phone;
    private String address;
    private City city;
    private String avatarUrl;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
