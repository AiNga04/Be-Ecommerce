package com.zyna.dev.ecommerce.users.dto.request;

import com.zyna.dev.ecommerce.common.enums.City;
import com.zyna.dev.ecommerce.common.enums.Gender;
import com.zyna.dev.ecommerce.common.enums.Role;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName; // optional

    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName; // optional

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth; // optional

    private Gender gender; // optional

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password; // optional

    private Set<String> roles;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String phone; // optional

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address; // optional

    private City city; // optional
}
