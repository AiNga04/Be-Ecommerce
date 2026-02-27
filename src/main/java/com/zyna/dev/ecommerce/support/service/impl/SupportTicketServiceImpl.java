package com.zyna.dev.ecommerce.support.service.impl;

import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.support.dto.request.SupportTicketReplyRequest;
import com.zyna.dev.ecommerce.support.dto.request.SupportTicketRequest;
import com.zyna.dev.ecommerce.support.dto.response.SupportTicketResponse;
import com.zyna.dev.ecommerce.support.models.SupportStatus;
import com.zyna.dev.ecommerce.support.models.SupportSubject;
import com.zyna.dev.ecommerce.support.models.SupportTicket;
import com.zyna.dev.ecommerce.support.repository.SupportTicketRepository;
import com.zyna.dev.ecommerce.support.service.SupportTicketService;
import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Override
    @Transactional
    public void submitTicket(SupportTicketRequest request) {
        SupportTicket ticket = SupportTicket.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(SupportStatus.PENDING)
                .build();
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getAllTickets(SupportSubject subject, SupportStatus status, Long processedById, String keyword, Pageable pageable) {
        return ticketRepository.findAllWithFilters(subject, status, processedById, keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(Long id) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Support ticket not found"));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public void updateTicketStatus(Long id, SupportStatus status) {
        SupportTicket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Support ticket not found"));
        
        ticket.setStatus(status);
        if (status == SupportStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        
        // Track processor
        setProcessor(ticket);
        
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public void replyTicket(Long ticketId, SupportTicketReplyRequest request) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Support ticket not found"));
        
        ticket.setReplyMessage(request.getReplyMessage());
        ticket.setInternalNote(request.getInternalNote());
        
        if (request.isMarkAsResolved()) {
            ticket.setStatus(SupportStatus.RESOLVED);
            ticket.setResolvedAt(LocalDateTime.now());
        }
        
        // Track processor
        setProcessor(ticket);
        
        ticketRepository.save(ticket);
        
        // Send email notification to customer
        if (request.getReplyMessage() != null && !request.getReplyMessage().isBlank()) {
            mailService.sendSupportReplyEmail(ticket, request.getReplyMessage());
        }
    }

    private void setProcessor(SupportTicket ticket) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Current user not found"));
        ticket.setProcessedBy(currentUser);
    }

    private SupportTicketResponse mapToResponse(SupportTicket ticket) {
        SupportTicketResponse response = SupportTicketResponse.builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .phone(ticket.getPhone())
                .email(ticket.getEmail())
                .subject(ticket.getSubject())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .internalNote(ticket.getInternalNote())
                .replyMessage(ticket.getReplyMessage())
                .resolvedAt(ticket.getResolvedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
        
        if (ticket.getProcessedBy() != null) {
            response.setProcessedById(ticket.getProcessedBy().getId());
            response.setProcessedByName(ticket.getProcessedBy().getFirstName() + " " + ticket.getProcessedBy().getLastName());
        }
        
        return response;
    }
}
