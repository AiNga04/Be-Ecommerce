package com.zyna.dev.ecommerce.common.mail;

import com.zyna.dev.ecommerce.users.models.User;
import com.zyna.dev.ecommerce.products.models.Product;
import com.zyna.dev.ecommerce.support.models.SupportTicket;
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

    public void sendActivationEmail(User user, String activationLink, com.zyna.dev.ecommerce.common.enums.ActivationType type) {
        String subject;
        String htmlContent;

        switch (type) {
            case RESTORE -> {
                subject = "Khôi phục tài khoản thành công";
                htmlContent = buildActivationRestoreHtml(user, activationLink);
            }
            case ADMIN_CREATE -> {
                // Should not happen here usually, as password is required for admin create, handled by overloaded method
                subject = "Kích hoạt tài khoản";
                htmlContent = buildActivationHtml(user, activationLink);
            }
            default -> { // REGISTRATION
                subject = "Kích hoạt tài khoản của bạn";
                htmlContent = buildActivationHtml(user, activationLink);
            }
        }

        sendHtmlEmail(
                new String[]{user.getEmail()},
                subject,
                htmlContent
        );
    }

    public void sendActivationEmail(User user, String activationLink, String rawPassword, com.zyna.dev.ecommerce.common.enums.ActivationType type) {
        String subject = "Thông tin tài khoản mới";
        String htmlContent;

        if (type == com.zyna.dev.ecommerce.common.enums.ActivationType.ADMIN_CREATE) {
             htmlContent = buildActivationWithPasswordHtml(user, activationLink, rawPassword);
        } else {
             // Fallback
             htmlContent = buildActivationHtml(user, activationLink);
        }

        sendHtmlEmail(
                new String[]{user.getEmail()},
                subject,
                htmlContent
        );
    }
    
    public void sendTemplateEmail(String[] recipients, String subject, String body) {
        if (recipients == null || recipients.length == 0) {
            return;
        }
        sendHtmlEmail(recipients, subject, wrapPlainText(body));
    }

    public void sendFormattedEmail(String[] recipients, String subject, String htmlBody) {
        if (recipients == null || recipients.length == 0) {
            return;
        }
        sendHtmlEmail(recipients, subject, baseTemplate(subject, htmlBody));
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

    public void sendSupportReplyEmail(SupportTicket ticket, String replyMessage) {
        if (!StringUtils.hasText(ticket.getEmail())) {
            return;
        }
        String subject = "Phản hồi yêu cầu hỗ trợ #" + ticket.getId();
        sendHtmlEmail(
                new String[]{ticket.getEmail()},
                subject,
                buildSupportReplyHtml(ticket, replyMessage)
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
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại Zyna. Vui lòng xác minh địa chỉ email của bạn để bắt đầu mua sắm:</p>"
                        + "<p style=\"text-align:center;margin:24px 0;\">"
                        + "<a href=\"" + activationLink + "\" style=\"background:#2563eb;color:#fff;"
                        + "padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;\">Xác minh Email</a>"
                        + "</p>"
                        + "<p>Email này có hiệu lực trong 24 giờ via.</p>"
        );
    }

    private String buildActivationRestoreHtml(User user, String activationLink) {
        String greeting = StringUtils.hasText(user.getFirstName())
                ? "Xin chào " + user.getFirstName() + ","
                : "Xin chào,";
        return baseTemplate(
                greeting,
                "<p>Tài khoản của bạn đã được khôi phục thành công trên hệ thống Zyna.</p>"
                        + "<p>Để đảm bảo bảo mật, vui lòng xác minh lại email của bạn để kích hoạt lại tài khoản:</p>"
                        + "<p style=\"text-align:center;margin:24px 0;\">"
                        + "<a href=\"" + activationLink + "\" style=\"background:#10b981;color:#fff;"
                        + "padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;\">Kích hoạt lại tài khoản</a>"
                        + "</p>"
                        + "<p>Nếu bạn không yêu cầu khôi phục, vui lòng liên hệ bộ phận hỗ trợ ngay lập tức.</p>"
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

    private String buildSupportReplyHtml(SupportTicket ticket, String replyMessage) {
        String greeting = "Xin chào " + ticket.getName() + ",";
        String safeReply = replyMessage.replace("\n", "<br/>");
        
        return baseTemplate(
                "Phản hồi hỗ trợ khách hàng",
                "<p>" + greeting + "</p>"
                        + "<p>Chúng tôi đã nhận được yêu cầu hỗ trợ của bạn về chủ đề: <strong>" + ticket.getSubject() + "</strong></p>"
                        + "<div style=\"background:#ffffff; border-left:4px solid #60a5fa; padding:16px; margin:20px 0; color:#334155; font-style:italic;\">"
                        + safeReply
                        + "</div>"
                        + "<p>Hy vọng phản hồi này giải đáp được thắc mắc của bạn. Nếu có thêm câu hỏi, đừng ngần ngại liên hệ lại với chúng tôi.</p>"
                        + "<p>Trân trọng,<br/>Đội ngũ hỗ trợ Zyna</p>"
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
