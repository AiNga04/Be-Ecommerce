package com.zyna.dev.ecommerce.support.dto.response;

import com.zyna.dev.ecommerce.support.models.SupportStatus;
import com.zyna.dev.ecommerce.support.models.SupportSubject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketResponse {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private SupportSubject subject;
    private String message;
    private SupportStatus status;
    private Long processedById;
    private String processedByName;
    private String internalNote;
    private String replyMessage;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
