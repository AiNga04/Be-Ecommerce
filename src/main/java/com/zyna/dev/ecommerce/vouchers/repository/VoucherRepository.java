package com.zyna.dev.ecommerce.vouchers.repository;

import com.zyna.dev.ecommerce.common.enums.VoucherStatus;
import com.zyna.dev.ecommerce.vouchers.models.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Voucher> findByCodeIgnoreCase(String code);

    Optional<Voucher> findByCodeIgnoreCaseAndStatus(String code, VoucherStatus status);

    Page<Voucher> findAllByStatus(VoucherStatus status, Pageable pageable);
}

