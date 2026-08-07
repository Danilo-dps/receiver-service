package br.com.danilodps.receiver.controller;

import br.com.danilodps.receiver.application.service.WebhookProcessorService;
import br.com.danilodps.receiver.domain.EventRequest;
import br.com.danilodps.receiver.infrastructure.security.WebhookSignatureVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookProcessorService processor;
    private final WebhookSignatureVerifier signatureVerifier;

    @PostMapping("/pedido-evento")
    public ResponseEntity<Void> receberWebhook(
            @RequestHeader("X-Webhook-Signature") String signature,
            @RequestHeader("X-Webhook-Id") String eventId,
            @RequestBody EventRequest evento) {

        log.info("[Receiver] Webhook recebido: {}", eventId);

        if (!signatureVerifier.verifySignature(evento, signature)) {
            log.error("[Receiver] Assinatura inválida para evento {}", eventId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        processor.process(evento);
        return ResponseEntity.ok().build();
    }

}