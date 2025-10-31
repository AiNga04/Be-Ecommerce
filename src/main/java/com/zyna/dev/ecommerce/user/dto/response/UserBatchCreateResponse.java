package com.zyna.dev.ecommerce.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBatchCreateResponse {

    // những user tạo thành công
    private List<UserResponse> created;

    // những email bị trùng / lỗi
    private List<FailedUser> failed;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailedUser {
        private String email;
        private String reason;
    }
}
