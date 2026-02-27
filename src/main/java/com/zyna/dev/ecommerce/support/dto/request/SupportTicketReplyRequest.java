package com.zyna.dev.ecommerce.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketReplyRequest {

    @NotBlank(message = "Reply message is required")
    private String replyMessage;

    private String internalNote;

    @Builder.Default
    private boolean markAsResolved = true;
}
