package com.zyna.dev.ecommerce.payments.vnpay;

import com.zyna.dev.ecommerce.common.enums.OrderStatus;
import com.zyna.dev.ecommerce.common.enums.PaymentMethod;
import com.zyna.dev.ecommerce.common.enums.PaymentStatus;
import com.zyna.dev.ecommerce.common.exceptions.ApplicationException;
import com.zyna.dev.ecommerce.orders.models.Order;
import com.zyna.dev.ecommerce.orders.repository.OrderRepository;
import com.zyna.dev.ecommerce.payments.vnpay.dto.VnPayReturnResponse;
import com.zyna.dev.ecommerce.payments.models.PaymentTransactionLog;
import com.zyna.dev.ecommerce.payments.repository.PaymentTransactionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig config;
    private final OrderRepository orderRepository;
    private final PaymentTransactionLogRepository paymentTransactionLogRepository;

    /**
     * Tạo URL thanh toán VNPay cho một order
     */
    public String createPaymentUrl(Long orderId, HttpServletRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Order already paid");
        }

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Order payment method is not ONLINE");
        }

        // Đảm bảo có order code
        if (order.getCode() == null || order.getCode().isBlank()) {
            order.setCode("ORD-" + order.getId());
            // 🔴 QUAN TRỌNG: LƯU LẠI
            order = orderRepository.save(order);
        }

        long amount = order.getTotalPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, String> params = config.baseParams();

        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_TxnRef", order.getCode());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getCode());
        params.put("vnp_IpAddr", request.getRemoteAddr());

        String hashData = VnPayUtil.generateQuery(params, false);
        String secureHash = VnPayUtil.hmacSHA512(config.getHashSecret(), hashData);

        String queryUrl = VnPayUtil.generateQuery(params, true)
                + "&vnp_SecureHash=" + secureHash;

        log.info("VnPay Query URL: {}", queryUrl);
        log.info("VnPay Query Params: {}", params);

        return config.getPayUrl() + "?" + queryUrl;
    }


    /**
     * Validate chữ ký VNPay từ vnp_* params
     */
    public boolean validateSignature(Map<String, String> vnpParams) {
        String receivedHash = vnpParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }

        // copy map, bỏ các field hash
        Map<String, String> params = new HashMap<>();
        vnpParams.forEach((k, v) -> {
            if (k.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(k)
                    && !"vnp_SecureHashType".equals(k)) {
                params.put(k, v);
            }
        });

        // build hashData giống lúc tạo URL: sort + encode value, không encode key
        String hashData = VnPayUtil.generateQuery(params, false);

        String expectedHash = VnPayUtil.hmacSHA512(config.getHashSecret(), hashData);

        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    /**
     * Xử lý kết quả thanh toán khi VNPay redirect về (vnp_ReturnUrl)
     */
    public VnPayReturnResponse handleReturn(Map<String, String> vnpParams) {

        Map<String, String> paramsCopy = new HashMap<>(vnpParams);
        boolean valid = validateSignature(paramsCopy);
        if (!valid) {
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Invalid VNPay signature");
        }

        String txnRef = vnpParams.get("vnp_TxnRef");               // order code
        String rspCode = vnpParams.get("vnp_ResponseCode");        // 00 = success
        String transactionStatus = vnpParams.get("vnp_TransactionStatus"); // 00 = success
        String transactionNo = vnpParams.get("vnp_TransactionNo");
        String bankCode = vnpParams.get("vnp_BankCode");
        String amountStr = vnpParams.get("vnp_Amount");
        String payDateStr = vnpParams.get("vnp_PayDate");

        Order order = orderRepository.findByCode(txnRef)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "Order not found"));

        // convert amount về BigDecimal (VNPay gửi *100)
        BigDecimal paidAmount = null;
        if (amountStr != null) {
            long amountLong = Long.parseLong(amountStr);
            paidAmount = BigDecimal.valueOf(amountLong).divide(BigDecimal.valueOf(100));
        }

        boolean success = "00".equals(rspCode) && "00".equals(transactionStatus);

        if (success) {
            order.markPaidByVnPay(
                    transactionNo,
                    rspCode,
                    paidAmount != null ? paidAmount : order.getTotalPrice(),
                    vnpParams.toString()
            );

            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
            }
        } else {
            order.markPaymentFailed("VNPAY", rspCode, vnpParams.toString());
        }

        orderRepository.save(order);
        savePaymentLog(order, success ? PaymentStatus.PAID : PaymentStatus.FAILED, paidAmount, transactionNo, rspCode, vnpParams.toString());

        return VnPayReturnResponse.builder()
                .orderCode(order.getCode())
                .paymentStatus(order.getPaymentStatus())
                .vnpResponseCode(rspCode)
                .vnpTransactionStatus(transactionStatus)
                .vnpTransactionNo(transactionNo)
                .vnpBankCode(bankCode)
                .vnpAmount(amountStr)
                .vnpPayDate(payDateStr)
                .message(success ? "Thanh toán thành công" : "Thanh toán thất bại")
                .build();
    }

    private void savePaymentLog(Order order,
                                PaymentStatus status,
                                BigDecimal amount,
                                String transactionId,
                                String responseCode,
                                String rawPayload) {
        PaymentTransactionLog logEntry = PaymentTransactionLog.builder()
                .order(order)
                .provider("VNPAY")
                .status(status)
                .amount(amount != null ? amount : order.getTotalPrice())
                .currency("VND")
                .transactionId(transactionId)
                .orderCode(order.getCode())
                .responseCode(responseCode)
                .rawPayload(rawPayload)
                .note(status == PaymentStatus.PAID ? "Payment success" : "Payment failed")
                .build();

        paymentTransactionLogRepository.save(logEntry);
    }
}
