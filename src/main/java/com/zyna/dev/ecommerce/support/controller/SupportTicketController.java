package com.zyna.dev.ecommerce.support.controller;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.support.dto.request.SupportTicketReplyRequest;
import com.zyna.dev.ecommerce.support.dto.request.SupportTicketRequest;
import com.zyna.dev.ecommerce.support.dto.response.SupportTicketResponse;
import com.zyna.dev.ecommerce.support.models.SupportStatus;
import com.zyna.dev.ecommerce.support.models.SupportSubject;
import com.zyna.dev.ecommerce.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;

    // Public endpoint to submit tickets
    @PostMapping("/support/tickets")
    public ResponseEntity<ApiResponse<Void>> submitTicket(@Valid @RequestBody SupportTicketRequest request) {
        ticketService.submitTicket(request);
        return ResponseEntity.ok(ApiResponse.successfulResponseNoData(HttpStatus.OK.value(), "Gửi yêu cầu hỗ trợ thành công"));
    }

    // Admin/Staff endpoints
    @GetMapping("/admin/support/tickets")
    @PreAuthorize("hasAuthority('SUPPORT_READ')")
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getAllTickets(
            @RequestParam(required = false) SupportSubject subject,
            @RequestParam(required = false) SupportStatus status,
            @RequestParam(required = false) Long processedById,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SupportTicketResponse> ticketsPage = ticketService.getAllTickets(subject, status, processedById, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.successfulPageResponse("Lấy danh sách yêu cầu hỗ trợ thành công", ticketsPage));
    }

    @GetMapping("/admin/support/tickets/{id}")
    @PreAuthorize("hasAuthority('SUPPORT_READ')")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.successfulResponse("Lấy thông tin yêu cầu hỗ trợ thành công", ticketService.getTicketById(id)));
    }

    @PutMapping("/admin/support/tickets/{id}/status")
    @PreAuthorize("hasAuthority('SUPPORT_WRITE')")
    public ResponseEntity<ApiResponse<Void>> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam SupportStatus status
    ) {
        ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.successfulResponseNoData(HttpStatus.OK.value(), "Cập nhật trạng thái yêu cầu hỗ trợ thành công"));
    }

    @PutMapping("/admin/support/tickets/{id}/reply")
    @PreAuthorize("hasAuthority('SUPPORT_WRITE')")
    public ResponseEntity<ApiResponse<Void>> replyTicket(
            @PathVariable Long id,
            @Valid @RequestBody SupportTicketReplyRequest request
    ) {
        ticketService.replyTicket(id, request);
        return ResponseEntity.ok(ApiResponse.successfulResponseNoData(HttpStatus.OK.value(), "Phản hồi yêu cầu hỗ trợ thành công"));
    }
}
