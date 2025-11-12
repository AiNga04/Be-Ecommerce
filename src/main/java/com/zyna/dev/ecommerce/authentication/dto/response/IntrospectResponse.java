package com.zyna.dev.ecommerce.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectResponse {
    private boolean valid;
    private String email;
}
