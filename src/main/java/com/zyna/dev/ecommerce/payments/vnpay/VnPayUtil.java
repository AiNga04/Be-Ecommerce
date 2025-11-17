package com.zyna.dev.ecommerce.payments.vnpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class VnPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);

            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            throw new RuntimeException("Cannot generate HMAC SHA512", ex);
        }
    }

    public static String generateQuery(Map<String, String> params, boolean encodeKey) {
        return params.entrySet().stream()
                .filter(p -> p.getValue() != null && !p.getValue().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .map(p -> (encodeKey
                        ? URLEncoder.encode(p.getKey(), StandardCharsets.US_ASCII)
                        : p.getKey()
                ) + "=" +
                        URLEncoder.encode(p.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
    }
}
