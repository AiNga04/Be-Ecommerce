package com.zyna.dev.ecommerce.support.service;

import com.zyna.dev.ecommerce.support.dto.request.SupportTicketReplyRequest;
import com.zyna.dev.ecommerce.support.dto.request.SupportTicketRequest;
import com.zyna.dev.ecommerce.support.dto.response.SupportTicketResponse;
import com.zyna.dev.ecommerce.support.models.SupportStatus;
import com.zyna.dev.ecommerce.support.models.SupportSubject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportTicketService {

    void submitTicket(SupportTicketRequest request);

    Page<SupportTicketResponse> getAllTickets(SupportSubject subject, SupportStatus status, Long processedById, String keyword, Pageable pageable);

    SupportTicketResponse getTicketById(Long id);

    void updateTicketStatus(Long id, SupportStatus status);

    void replyTicket(Long ticketId, SupportTicketReplyRequest request);
}
