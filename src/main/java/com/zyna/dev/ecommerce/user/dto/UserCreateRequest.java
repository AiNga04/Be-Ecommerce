package com.zyna.dev.ecommerce.user.dto;

import com.zyna.dev.ecommerce.common.enums.City;
import com.zyna.dev.ecommerce.common.enums.Gender;
import com.zyna.dev.ecommerce.common.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String password;
    private Role role;
    private String phone;
    private String address;
    private City city;
}
