package com.zyna.dev.ecommerce.users.service;

import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayNotificationJob {

    private final UserRepository userRepository;
    private final MailService mailService;

    @Value("${app.scheduler.birthday.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${app.scheduler.birthday.cron:0 0 6 * * ?}")
    public void sendBirthdayGreetings() {
        if (!enabled) {
            return;
        }

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        var users = userRepository.findByBirthDayMonthDay(month, day);
        if (users.isEmpty()) {
            return;
        }

        log.info("Sending birthday emails for {} users ({}-{})", users.size(), day, month);
        users.forEach(mailService::sendBirthdayEmail);
    }
}
