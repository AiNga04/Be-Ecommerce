package com.zyna.dev.ecommerce.payments.vnpay;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.payments.vnpay.dto.VnPayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payments/vnpay")
@RequiredArgsConstructor
public class VnPayPaymentController {

    private final VnPayService vnPayService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> createPayment(
            @RequestParam Long orderId,
            HttpServletRequest request
    ) {
        String url = vnPayService.createPaymentUrl(orderId, request);
        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "Create VNPay payment URL successfully!",
                url
        );
    }

    @GetMapping("/return")
    public void handleReturn(HttpServletRequest req, HttpServletResponse response) throws IOException {

        // Lọc các param bắt đầu bằng vnp_
        Map<String, String[]> raw = req.getParameterMap();
        Map<String, String> vnpParams = new HashMap<>();
        raw.forEach((k, v) -> {
            if (k.startsWith("vnp_") && v.length > 0) {
                vnpParams.put(k, v[0]);
            }
        });

        try {
            VnPayReturnResponse data = vnPayService.handleReturn(vnpParams);
            
            // Redirect về Frontend kèm status
            String frontendUrl = "http://localhost:3000/checkout/vnpay-return";
            String redirectUrl = frontendUrl + 
                "?status=" + (data.getMessage().contains("thành công") ? "success" : "error") +
                "&message=" + URLEncoder.encode(data.getMessage(), StandardCharsets.UTF_8) +
                "&orderCode=" + data.getOrderCode() +
                "&vnp_ResponseCode=" + data.getVnpResponseCode() +
                "&vnp_TransactionStatus=" + data.getVnpTransactionStatus();
                
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            // Redirect về FE báo lỗi
            String feUrl = "http://localhost:3000/checkout/vnpay-return";
            String redirectUrl = feUrl + "?" +
                    "status=error" +
                    "&message=" + URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Unknown error", StandardCharsets.UTF_8);
             
            response.sendRedirect(redirectUrl);
        }
    }
}
