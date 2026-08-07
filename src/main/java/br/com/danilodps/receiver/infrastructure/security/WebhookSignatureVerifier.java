package br.com.danilodps.receiver.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class WebhookSignatureVerifier {

    private final ObjectMapper objectMapper;
    private static final String HMAC_ALGO = "HmacSHA256";

    @Value("${webhook.secret:super-secreto-123}")
    private String secret;

    @SneakyThrows
    public boolean verifySignature(Object payload, String signatureReceived) {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), HMAC_ALGO);
        mac.init(secretKey);

        byte[] bytes = objectMapper.writeValueAsBytes(payload);
        byte[] calculatedSignature = mac.doFinal(bytes);
        String expectedSignature = Base64.getEncoder().encodeToString(calculatedSignature);

        return expectedSignature.equals(signatureReceived);
    }

}