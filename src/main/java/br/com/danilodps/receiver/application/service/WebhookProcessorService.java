package br.com.danilodps.receiver.application.service;

import br.com.danilodps.receiver.domain.EventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebhookProcessorService {

    // Idempotência em memória (produção: Redis ou banco)
    private final Set<String> eventosProcessados = ConcurrentHashMap.newKeySet();

    public void process(EventRequest event) {
        if (eventosProcessados.contains(event.eventId())) {
            log.warn("[Receiver] Evento {} já processado. Ignorando.", event.eventId());
            return;
        }

        log.info("[Receiver] ====================================");
        log.info("[Receiver] Processando evento: {}", event.eventId());
        log.info("[Receiver] Pedido: {} | Status: {} | Valor: R$ {}",
                event.orderId(), event.status(), event.price());
        log.info("[Receiver] ====================================");

        eventosProcessados.add(event.eventId());
    }
}
