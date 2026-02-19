package com.zyna.dev.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatResponse {
    private long totalUsers;
    private long activeUsers;
    private long disabledUsers; // Matches Status.DISABLED
    private long pendingUsers;
    private long newUsersToday;
}
