package com.startup.tasteflowbe.utils;

import com.startup.tasteflowbe.dto.PayOSWebhookDTO;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.security.MessageDigest;

public class PayOSWebhookUtil {
    private static String prepareDataString(PayOSWebhookDTO dto) {
        PayOSWebhookDTO.WebhookData data = dto.getData();

        StringBuilder dataString = new StringBuilder();

        dataString.append("amount=").append(data.getAmount() != null ? data.getAmount().toString() : "")
                .append("&description=").append(data.getDescription() != null ? data.getDescription() : "")
                .append("&orderCode=").append(data.getOrderCode() != null ? data.getOrderCode().toString() : "")
                .append("&state=").append(data.getState() != null ? data.getState() : "")
                .append("&transactionId=").append(data.getTransactionId() != null ? data.getTransactionId() : "")
                .append("&transTime=").append(data.getTransTime() != null ? data.getTransTime() : "");

        return dataString.toString();
    }



    public static boolean verifySignature(PayOSWebhookDTO dto, String checksumKey) {
        try {
            String dataString = prepareDataString(dto);
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKey);

            byte[] hash = sha256_HMAC.doFinal(dataString.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString().equalsIgnoreCase(dto.getSignature());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
