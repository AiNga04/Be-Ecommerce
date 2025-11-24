package com.zyna.dev.ecommerce.common.mail;

import com.zyna.dev.ecommerce.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@zyna.dev}")
    private String defaultFrom;

    public void sendActivationEmail(User user, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(defaultFrom);
        message.setSubject("Kích hoạt tài khoản của bạn");

        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";

        message.setText(greeting + "\n\n"
                + "Cảm ơn bạn đã đăng ký. Hãy nhấn vào liên kết dưới đây để kích hoạt tài khoản của bạn:\n"
                + activationLink + "\n\n"
                + "Nếu bạn không tạo tài khoản, vui lòng bỏ qua email này.");

        mailSender.send(message);
    }

    public void sendPasswordResetOtp(User user, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(defaultFrom);
        message.setSubject("Mã OTP đặt lại mật khẩu");

        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";

        message.setText(greeting + "\n\n"
                + "Mã OTP của bạn là: " + otpCode + "\n"
                + "OTP có hiệu lực trong thời gian ngắn, vui lòng không chia sẻ cho người khác.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.");

        mailSender.send(message);
    }
}
