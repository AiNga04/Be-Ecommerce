package com.zyna.dev.ecommerce.support.repository;

import com.zyna.dev.ecommerce.support.models.SupportStatus;
import com.zyna.dev.ecommerce.support.models.SupportSubject;
import com.zyna.dev.ecommerce.support.models.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @Query("SELECT t FROM SupportTicket t WHERE " +
            "(:subject IS NULL OR t.subject = :subject) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:processedById IS NULL OR t.processedBy.id = :processedById) AND " +
            "(:keyword IS NULL OR CAST(LOWER(t.name) AS string) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR CAST(LOWER(t.email) AS string) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR CAST(LOWER(t.phone) AS string) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) " +
            "OR CAST(LOWER(t.message) AS string) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    Page<SupportTicket> findAllWithFilters(
            @Param("subject") SupportSubject subject,
            @Param("status") SupportStatus status,
            @Param("processedById") Long processedById,
            @Param("keyword") String keyword,
            Pageable pageable);
}
