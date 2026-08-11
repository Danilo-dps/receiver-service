package br.com.danilodps.receiver.infrastructure.security;

import br.com.danilodps.receiver.domain.VerificationResult;
import br.com.danilodps.receiver.infrastructure.cache.DualSecretCache;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Component
public class WebhookSignatureVerifier {

    private final DualSecretCache secretCache;
    private static final String HMAC_ALGO = "HmacSHA256";

    @Value("${webhook.timestamp.tolerance}")
    private long timestampTolerance;

    public WebhookSignatureVerifier(DualSecretCache secretCache) {
        this.secretCache = secretCache;
    }

    @SneakyThrows
    public VerificationResult verifySignature(String signatureHeader, long timestamp, String eventId, String payload) {

        // 1. Valida timestamp
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > timestampTolerance) {
            return VerificationResult.fail("Timestamp fora da janela");
        }

        // 2. Parse do header: "v2=abc123..."
        String[] parts = signatureHeader.split("=", 2);
        if (parts.length != 2) {
            return VerificationResult.fail("Header mal formatado");
        }

        String version = parts[0];
        String signatureReceived = parts[1];

        // 3. Busca em current ou previous (O(1), duas comparações de referência)
        var cached = secretCache.findForValidation(version);
        if (cached == null) {
            return VerificationResult.fail("Versão desconhecida no cache: " + version);
        }

        String signedContent = String.format("%s.%d.%s.%s", version, timestamp, eventId, payload);

        // 4. Recalcula HMAC
        String expectedSignature = calculateHmac(signedContent, cached.secret());

        // 5. Comparação segura contra timing attack
        boolean valid = MessageDigest.isEqual(
                signatureReceived.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
        );

        return valid
                ? VerificationResult.ok(version)
                : VerificationResult.fail("Assinatura inválida");
    }

    @SneakyThrows
    private String calculateHmac(String content, String secret) {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        mac.init(key);
        return Base64.getEncoder().encodeToString(
                mac.doFinal(content.getBytes(StandardCharsets.UTF_8))
        );
    }

}