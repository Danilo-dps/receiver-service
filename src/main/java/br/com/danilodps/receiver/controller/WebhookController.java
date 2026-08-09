package br.com.danilodps.receiver.controller;

import br.com.danilodps.receiver.application.service.WebhookProcessorService;
import br.com.danilodps.receiver.domain.EventRequest;
import br.com.danilodps.receiver.infrastructure.security.WebhookSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final WebhookProcessorService processor;
    private final WebhookSignatureVerifier signatureVerifier;

    public WebhookController(WebhookProcessorService processor, WebhookSignatureVerifier signatureVerifier) {
        this.processor = processor;
        this.signatureVerifier = signatureVerifier;
    }

    @PostMapping("/order-event")
    public ResponseEntity<Void> receiverWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader("X-Webhook-Timestamp") long timestamp,
            @RequestHeader("X-Webhook-Id") String eventId,
            @RequestBody EventRequest event) {

        log.info("[Receiver] Webhook recebido: {} [ts: {}]", eventId, timestamp);

        var result = signatureVerifier.verifySignature(signature, timestamp, eventId, event);

        if (!result.valid()) {
            log.error("[Receiver] Rejeitado: {}", result.error());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("[Receiver] Assinatura válida (versão {})", result.version());
        processor.process(event);
        return ResponseEntity.ok().build();
    }

}