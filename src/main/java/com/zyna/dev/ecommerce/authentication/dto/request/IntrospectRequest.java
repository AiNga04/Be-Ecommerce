package com.zyna.dev.ecommerce.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectRequest {
    private String token;
    private boolean authenticated;
}
