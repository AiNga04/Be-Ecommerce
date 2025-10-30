package com.zyna.dev.ecommerce.auth.dto.response;

import com.zyna.dev.ecommerce.user.dto.UserResponse;
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
