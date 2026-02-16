package com.zyna.dev.ecommerce.users.dto.request;

import com.zyna.dev.ecommerce.common.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private Status status;
}
