package com.zyna.dev.ecommerce.vouchers.service;

import com.zyna.dev.ecommerce.common.enums.VoucherStatus;
import com.zyna.dev.ecommerce.notifications.NotificationService;
import com.zyna.dev.ecommerce.notifications.NotificationType;
import com.zyna.dev.ecommerce.users.UserRepository;
import com.zyna.dev.ecommerce.vouchers.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherExpiringJob {

    private final VoucherRepository voucherRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.scheduler.voucher-expiring.enabled:true}")
    private boolean enabled;

    @Value("${app.scheduler.voucher-expiring.days-before:3}")
    private int daysBefore;

    @Scheduled(cron = "${app.scheduler.voucher-expiring.cron:0 0 7 * * ?}")
    public void notifyExpiringVouchers() {
        if (!enabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusDays(daysBefore > 0 ? daysBefore : 3);

        var vouchers = voucherRepository.findByStatusAndEndDateBetween(
                VoucherStatus.ACTIVE,
                now,
                target
        );

        if (vouchers.isEmpty()) {
            return;
        }

        var admins = userRepository.findAllByRoles_CodeIgnoreCaseAndIsDeletedFalse("ADMIN")
                .stream().map(u -> u.getEmail()).toList();

        vouchers.forEach(v -> notificationService.sendEmail(
                NotificationType.VOUCHER_EXPIRING,
                admins,
                java.util.Map.of(
                        "code", v.getCode(),
                        "endDate", v.getEndDate()
                )
        ));

        log.info("Sent voucher expiring notifications for {} vouchers", vouchers.size());
    }
}
