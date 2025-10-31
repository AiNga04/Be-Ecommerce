package com.zyna.dev.ecommerce.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBatchCreateRequest {

    @NotEmpty(message = "User list must not be empty")
    @Valid
    private List<UserCreateRequest> users;
}
