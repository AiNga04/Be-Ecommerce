package com.zyna.dev.ecommerce.notifications;

import com.zyna.dev.ecommerce.common.mail.MailService;
import com.zyna.dev.ecommerce.users.models.User;
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

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendEmail(NotificationType type, Collection<String> recipients, Map<String, Object> model) {
        if (CollectionUtils.isEmpty(recipients)) {
            return;
        }
        NotificationPayload payload = buildPayload(type, model);
        // Use formatted email for HTML content validity
        mailService.sendFormattedEmail(recipients.toArray(new String[0]), payload.getSubject(), payload.getBody());
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
                String shippingTotal = stringVal(model.get("shippingFee"));
                String discountTotal = stringVal(model.get("discountAmount"));
                String shippingName = stringVal(model.get("shippingName"));
                String shippingPhone = stringVal(model.get("shippingPhone"));
                String shippingAddress = stringVal(model.get("shippingAddress"));
                String paymentMethod = stringVal(model.get("paymentMethod"));
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) model.get("items");

                subject = "Xác nhận đơn hàng " + (orderCode != null ? "#" + orderCode : "");
                
                StringBuilder sb = new StringBuilder();
                sb.append("<p>Xin chào <strong>").append(shippingName).append("</strong>,</p>");
                sb.append("<p>Cảm ơn bạn đã đặt hàng tại Zyna! Đơn hàng của bạn đã được tiếp nhận và đang trong quá trình xử lý.</p>");
                
                // ORDER INFO CARD
                sb.append("<div style=\"background:#f1f5f9; padding: 16px; border-radius: 8px; margin-bottom: 24px;\">");
                sb.append("<p style=\"margin:4px 0;\"><strong>Mã đơn hàng:</strong> #").append(orderCode).append("</p>");
                sb.append("<p style=\"margin:4px 0;\"><strong>Ngày đặt:</strong> ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))).append("</p>");
                sb.append("<p style=\"margin:4px 0;\"><strong>Phương thức thanh toán:</strong> ").append(paymentMethod).append("</p>");
                sb.append("</div>");

                // ITEM TABLE
                sb.append("<table style=\"width:100%; border-collapse: collapse; margin-bottom: 24px;\">");
                sb.append("<thead><tr style=\"border-bottom: 2px solid #e2e8f0;\">");
                sb.append("<th style=\"text-align:left; padding: 12px 0; color: #64748b;\">Sản phẩm</th>");
                sb.append("<th style=\"text-align:center; padding: 12px 0; color: #64748b;\">Số lượng</th>");
                sb.append("<th style=\"text-align:right; padding: 12px 0; color: #64748b;\">Giá</th>");
                sb.append("</tr></thead><tbody>");

                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String pName = stringVal(item.get("productName"));
                        String pSize = stringVal(item.get("size"));
                        String pQty = stringVal(item.get("quantity"));
                        String pPrice = stringVal(item.get("subtotal")); // Show subtotal per line
                        String pImage = formatImageUrl(stringVal(item.get("image")));

                        sb.append("<tr style=\"border-bottom: 1px solid #f1f5f9;\">");
                        sb.append("<td style=\"padding: 12px 0;\">");
                        sb.append("<div style=\"display:flex; align-items:center;\">");
                        if (StringUtils.hasText(pImage)) {
                            sb.append("<img src=\"").append(pImage).append("\" style=\"width:48px; height:48px; object-fit:cover; border-radius:4px; margin-right:12px;\"/>");
                        }
                        sb.append("<div>");
                        sb.append("<div style=\"font-weight:600; color:#0f172a;\">").append(pName).append("</div>");
                        sb.append("<div style=\"color:#64748b; font-size:13px;\">Size: ").append(pSize).append("</div>");
                        sb.append("</div></div></td>");
                        sb.append("<td style=\"text-align:center; padding: 12px 0;\">x").append(pQty).append("</td>");
                        sb.append("<td style=\"text-align:right; padding: 12px 0; font-weight:600;\">").append(formatMoney(pPrice)).append(" đ</td>");
                        sb.append("</tr>");
                    }
                }
                sb.append("</tbody></table>");

                // SUMMARY
                sb.append("<div style=\"max-width: 250px; margin-left: auto;\">");
                sb.append("<div style=\"display:flex; justify-content:space-between; margin-bottom: 8px;\">");
                sb.append("<span style=\"color:#64748b;\">Tạm tính:</span>");
                // Need to calc total subtotal or just pass it? Using total for now logic
                sb.append("<span>...</span>"); 
                sb.append("</div>");
                
                if (shippingTotal != null) {
                    sb.append("<div style=\"display:flex; justify-content:space-between; margin-bottom: 8px;\">");
                    sb.append("<span style=\"color:#64748b;\">Phí vận chuyển:</span>");
                    sb.append("<span>").append(formatMoney(shippingTotal)).append(" đ</span>");
                    sb.append("</div>");
                }
                 if (discountTotal != null && !discountTotal.equals("0")) {
                    sb.append("<div style=\"display:flex; justify-content:space-between; margin-bottom: 8px;\">");
                    sb.append("<span style=\"color:#64748b;\">Giảm giá:</span>");
                    sb.append("<span style=\"color:#ef4444;\">-").append(formatMoney(discountTotal)).append(" đ</span>");
                    sb.append("</div>");
                }
                
                sb.append("<div style=\"border-top: 2px solid #e2e8f0; padding-top: 12px; display:flex; justify-content:space-between; font-size: 18px; font-weight: 700; color: #0f172a;\">");
                sb.append("<span>Tổng cộng:</span>");
                sb.append("<span>").append(formatMoney(total)).append(" đ</span>");
                sb.append("</div>");
                sb.append("</div>");

                // ADDRESS INFO
                sb.append("<div style=\"margin-top: 32px; border-top: 1px dashed #cbd5e1; padding-top: 24px;\">");
                sb.append("<h3 style=\"font-size: 16px; margin: 0 0 12px;\">Thông tin giao hàng</h3>");
                sb.append("<p style=\"margin: 4px 0;\"><strong>").append(shippingName).append("</strong></p>");
                sb.append("<p style=\"margin: 4px 0; color: #64748b;\">").append(shippingPhone).append("</p>");
                sb.append("<p style=\"margin: 4px 0; color: #64748b;\">").append(shippingAddress).append("</p>");
                sb.append("</div>");

                // BUTTON
                sb.append("<div style=\"text-align:center; margin-top: 32px;\">");
                sb.append("<a href=\"http://localhost:3000/orders/").append(orderCode).append("\" style=\"background: #2563eb; color: #fff; padding: 12px 24px; border-radius: 6px; text-decoration: none; font-weight: 600;\">Xem chi tiết đơn hàng</a>");
                sb.append("</div>");

                body = sb.toString();
            }
            case LOW_STOCK_ALERT -> {
                String productName = stringVal(model.get("productName"));
                String stock = stringVal(model.get("stock"));
                subject = "Cảnh báo tồn kho thấp";
                body = "<p>Sản phẩm: <strong>" + productName + "</strong></p>"
                        + "<p>Tồn kho hiện tại: <strong style='color:red'>" + stock + "</strong></p>"
                        + "<p>Vui lòng nhập thêm hàng.</p>";
            }
            case VOUCHER_ACTIVATED -> {
                String code = stringVal(model.get("code"));
                String endDate = stringVal(model.get("endDate"));
                subject = "Voucher mới đã được kích hoạt";
                body = "<p>Voucher <strong>" + code + "</strong> đã được kích hoạt.</p>"
                        + (StringUtils.hasText(endDate) ? "<p>Hiệu lực đến: " + endDate + "</p>" : "");
            }
            case VOUCHER_EXPIRING -> {
                String code = stringVal(model.get("code"));
                String endDate = stringVal(model.get("endDate"));
                subject = "Voucher sắp hết hạn";
                body = "<p>Voucher <strong>" + code + "</strong> sắp hết hạn.</p>"
                        + (StringUtils.hasText(endDate) ? "<p>Hiệu lực đến: " + endDate + "</p>" : "");
            }
            case BIRTHDAY -> {
                subject = "Chúc mừng sinh nhật!";
                body = "<p>Zyna chúc bạn một ngày sinh nhật vui vẻ và nhiều ưu đãi.</p>";
            }
            default -> {
                subject = "Thông báo từ Zyna";
                body = "<p>Bạn có một thông báo mới.</p>";
            }
        }
        return NotificationPayload.builder()
                .type(type)
                .subject(subject)
                .body(body)
                .build();
    }
    
    private String formatMoney(String val) {
        if (val == null) return "0";
        try {
            double d = Double.parseDouble(val);
            return String.format("%,.0f", d);
        } catch (Exception e) {
            return val;
        }
    }
    
    private String formatImageUrl(String url) {
        if (!StringUtils.hasText(url)) return "";
        if (url.startsWith("http")) return url;
        
        String cleanUrl = url.startsWith("/") ? url : "/" + url;
        // remove trailing slash from baseUrl if exists to avoid double slash, though redundant if convention kept
        String cleanBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        
        return cleanBase + cleanUrl;
    }

    private String stringVal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.time.LocalDateTime ldt) {
            return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return obj.toString();
    }
}
