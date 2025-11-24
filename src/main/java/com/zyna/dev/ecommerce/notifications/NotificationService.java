package com.zyna.dev.ecommerce.notifications;

import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MailService mailService;

    public void sendEmail(NotificationType type, User to, Map<String, Object> model) {
        if (to == null || !StringUtils.hasText(to.getEmail())) {
            return;
        }
        sendEmail(type, java.util.List.of(to.getEmail()), model);
    }

    public void sendEmail(NotificationType type, Collection<String> recipients, Map<String, Object> model) {
        if (CollectionUtils.isEmpty(recipients)) {
            return;
        }
        NotificationPayload payload = buildPayload(type, model);
        mailService.sendTemplateEmail(recipients.toArray(new String[0]), payload.getSubject(), payload.getBody());
    }

    // placeholder for future in-app notifications
    public void sendInApp(NotificationType type, User to, Map<String, Object> model) {
        log.debug("In-app notification [{}] to {}: {}", type, to != null ? to.getEmail() : "unknown", model);
    }

    private NotificationPayload buildPayload(NotificationType type, Map<String, Object> model) {
        String subject;
        String body;
        switch (type) {
            case ORDER_PLACED -> {
                String orderCode = stringVal(model.get("orderCode"));
                String total = stringVal(model.get("total"));
                String shippingName = stringVal(model.get("shippingName"));
                String shippingPhone = stringVal(model.get("shippingPhone"));
                String shippingAddress = stringVal(model.get("shippingAddress"));
                String paymentMethod = stringVal(model.get("paymentMethod"));

                subject = "Đơn hàng " + (orderCode != null ? orderCode : "") + " đã được tạo";
                StringBuilder sb = new StringBuilder();
                sb.append("Đơn hàng ").append(orderCode != null ? orderCode : "").append(" đã được tạo thành công.\n")
                        .append("Tổng tiền: ").append(total != null ? total : "N/A").append("\n");
                if (paymentMethod != null) {
                    sb.append("Phương thức thanh toán: ").append(paymentMethod).append("\n");
                }
                if (shippingName != null) {
                    sb.append("Người nhận: ").append(shippingName);
                    if (shippingPhone != null) sb.append(" | ").append(shippingPhone);
                    sb.append("\n");
                }
                if (shippingAddress != null) {
                    sb.append("Địa chỉ: ").append(shippingAddress).append("\n");
                }
                sb.append("Cảm ơn bạn đã mua sắm tại Zyna.");
                body = sb.toString();
            }
            case LOW_STOCK_ALERT -> {
                String productName = stringVal(model.get("productName"));
                String stock = stringVal(model.get("stock"));
                subject = "Cảnh báo tồn kho thấp";
                body = "Sản phẩm: " + productName + "\n"
                        + "Tồn kho hiện tại: " + stock + "\n"
                        + "Vui lòng nhập thêm hàng.";
            }
            case VOUCHER_ACTIVATED -> {
                String code = stringVal(model.get("code"));
                String endDate = stringVal(model.get("endDate"));
                subject = "Voucher mới đã được kích hoạt";
                body = "Voucher " + code + " đã được kích hoạt.\n"
                        + (StringUtils.hasText(endDate) ? "Hiệu lực đến: " + endDate : "");
            }
            case VOUCHER_EXPIRING -> {
                String code = stringVal(model.get("code"));
                String endDate = stringVal(model.get("endDate"));
                subject = "Voucher sắp hết hạn";
                body = "Voucher " + code + " sắp hết hạn.\n"
                        + (StringUtils.hasText(endDate) ? "Hiệu lực đến: " + endDate : "");
            }
            case BIRTHDAY -> {
                subject = "Chúc mừng sinh nhật!";
                body = "Zyna chúc bạn một ngày sinh nhật vui vẻ và nhiều ưu đãi.";
            }
            default -> {
                subject = "Thông báo từ Zyna";
                body = "Bạn có một thông báo mới.";
            }
        }
        return NotificationPayload.builder()
                .type(type)
                .subject(subject)
                .body(body)
                .build();
    }

    private String stringVal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime ldt) {
            return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return obj.toString();
    }
}
