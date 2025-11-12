package com.zyna.dev.ecommerce.authentication.dto.response;

import com.zyna.dev.ecommerce.users.dto.response.UserResponse;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserResponse user;
}
