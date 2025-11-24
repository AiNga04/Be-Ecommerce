package com.zyna.dev.ecommerce.common.mail;

import com.zyna.dev.ecommerce.users.User;
import com.zyna.dev.ecommerce.products.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@zyna.dev}")
    private String defaultFrom;

    public void sendActivationEmail(User user, String activationLink) {
        sendHtmlEmail(
                new String[]{user.getEmail()},
                "Kích hoạt tài khoản của bạn",
                buildActivationHtml(user, activationLink)
        );
    }

    public void sendActivationEmail(User user, String activationLink, String rawPassword) {
        sendHtmlEmail(
                new String[]{user.getEmail()},
                "Kích hoạt tài khoản của bạn",
                buildActivationWithPasswordHtml(user, activationLink, rawPassword)
        );
    }

    public void sendTemplateEmail(String[] recipients, String subject, String body) {
        if (recipients == null || recipients.length == 0) {
            return;
        }
        sendHtmlEmail(recipients, subject, wrapPlainText(body));
    }

    public void sendPasswordResetOtp(User user, String otpCode) {
        sendHtmlEmail(
                new String[]{user.getEmail()},
                "Mã OTP đặt lại mật khẩu",
                buildOtpHtml(user, otpCode)
        );
    }

    public void sendBirthdayEmail(User user) {
        sendHtmlEmail(
                new String[]{user.getEmail()},
                "Chúc mừng sinh nhật!",
                buildBirthdayHtml(user)
        );
    }

    public void sendLowStockAlert(Product product, int stock, String[] recipients) {
        if (recipients == null || recipients.length == 0) {
            return;
        }
        sendHtmlEmail(
                recipients,
                "Cảnh báo tồn kho thấp: " + product.getName(),
                buildLowStockHtml(product, stock)
        );
    }

    private void sendHtmlEmail(String[] recipients, String subject, String htmlContent) {
        if (recipients == null || recipients.length == 0) {
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(recipients);
            helper.setFrom(defaultFrom);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (Exception ignored) {
        }
    }

    private String buildActivationHtml(User user, String activationLink) {
        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";
        return baseTemplate(
                greeting,
                "<p>Cảm ơn bạn đã đăng ký. Nhấn vào nút dưới đây để kích hoạt tài khoản:</p>"
                        + "<p style=\"text-align:center;margin:24px 0;\">"
                        + "<a href=\"" + activationLink + "\" style=\"background:#2563eb;color:#fff;"
                        + "padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;\">Kích hoạt tài khoản</a>"
                        + "</p>"
                        + "<p>Nếu bạn không tạo tài khoản, hãy bỏ qua email này.</p>"
        );
    }

    private String buildActivationWithPasswordHtml(User user, String activationLink, String rawPassword) {
        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";
        return baseTemplate(
                greeting,
                "<p>Tài khoản của bạn đã được admin tạo. Dùng mật khẩu tạm bên dưới để đăng nhập sau khi kích hoạt:</p>"
                        + "<div style=\"font-size:20px;font-weight:700;color:#22d3ee;margin:16px 0;\">"
                        + rawPassword + "</div>"
                        + "<p style=\"text-align:center;margin:24px 0;\">"
                        + "<a href=\"" + activationLink + "\" style=\"background:#2563eb;color:#fff;"
                        + "padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;\">Kích hoạt tài khoản</a>"
                        + "</p>"
                        + "<p>Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu để đảm bảo an toàn.</p>"
        );
    }

    private String buildOtpHtml(User user, String otpCode) {
        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";
        return baseTemplate(
                greeting,
                "<p>Mã OTP đặt lại mật khẩu của bạn là:</p>"
                        + "<div style=\"font-size:22px;font-weight:700;color:#0f172a;margin:16px 0;\">"
                        + otpCode + "</div>"
                        + "<p>OTP có hiệu lực trong thời gian ngắn, vui lòng không chia sẻ cho người khác.</p>"
        );
    }

    private String buildBirthdayHtml(User user) {
        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";
        return baseTemplate(
                greeting,
                "<p>Chúc mừng sinh nhật! Zyna gửi tới bạn lời chúc tốt đẹp nhất và những ưu đãi dành riêng cho ngày đặc biệt này.</p>"
                        + "<p>Chúc bạn một ngày thật vui vẻ và mua sắm nhiều niềm vui!</p>"
        );
    }

    private String buildLowStockHtml(Product product, int stock) {
        return baseTemplate(
                "Cảnh báo tồn kho thấp",
                "<p>Sản phẩm: <strong>" + product.getName() + "</strong></p>"
                        + "<p>ID: " + product.getId() + "</p>"
                        + "<p>Tồn kho hiện tại: <strong style=\"color:#b91c1c;\">" + stock + "</strong></p>"
                        + "<p>Vui lòng nhập thêm hàng để tránh hết hàng.</p>"
        );
    }

    private String wrapPlainText(String body) {
        String safeBody = body.replace("\n", "<br/>");
        return baseTemplate("Thông báo", "<p>" + safeBody + "</p>");
    }

    private String baseTemplate(String title, String contentHtml) {
        return """
                <div style="font-family:Arial,Helvetica,sans-serif; background:#f4f6fb; padding:32px;">
                  <div style="max-width:700px;margin:0 auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:16px;overflow:hidden;box-shadow:0 12px 32px rgba(15,23,42,0.08);">
                    <div style="background:linear-gradient(120deg,#60a5fa,#7c3aed);color:#ffffff;padding:18px 22px;font-size:18px;font-weight:700;letter-spacing:0.2px;">
                      Zyna
                    </div>
                    <div style="padding:28px 26px;color:#0f172a;font-size:15px;line-height:1.65;">
                      <h2 style="margin:0 0 14px;font-size:22px;font-weight:700;color:#0f172a;">""" + title + """
                      </h2>
                      <div style="background:#f8fafc;border:1px solid #e5e7eb;border-radius:12px;padding:18px 18px 12px;margin-top:10px;box-shadow:0 6px 16px rgba(15,23,42,0.06);">
                        """ + contentHtml + """
                      </div>
                    </div>
                    <div style="padding:16px 20px;font-size:12px;color:#6b7280;background:#f9fafb;border-top:1px solid #e5e7eb;text-align:center;">
                      Đây là email tự động, vui lòng không trả lời.
                    </div>
                  </div>
                </div>
                """;
    }
}
