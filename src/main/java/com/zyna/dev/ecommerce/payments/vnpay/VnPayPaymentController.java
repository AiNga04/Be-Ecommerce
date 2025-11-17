package com.zyna.dev.ecommerce.payments.vnpay;

import com.zyna.dev.ecommerce.common.ApiResponse;
import com.zyna.dev.ecommerce.payments.vnpay.dto.VnPayReturnResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VnPayReturnResponse> handleReturn(HttpServletRequest req) {

        // Lọc các param bắt đầu bằng vnp_
        Map<String, String[]> raw = req.getParameterMap();
        Map<String, String> vnpParams = new HashMap<>();
        raw.forEach((k, v) -> {
            if (k.startsWith("vnp_") && v.length > 0) {
                vnpParams.put(k, v[0]);
            }
        });

        VnPayReturnResponse data = vnPayService.handleReturn(vnpParams);

        return ApiResponse.successfulResponse(
                HttpStatus.OK.value(),
                "VNPay payment result",
                data
        );
    }
}
