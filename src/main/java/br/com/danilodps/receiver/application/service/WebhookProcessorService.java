package br.com.danilodps.receiver.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WebhookProcessorService {

    // Idempotência em memória (produção: Redis ou banco)
    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();

    public void process(String eventId) {

        if (processedEvents.contains(eventId)) {
            log.warn("[Receiver] Evento {} já processado. Ignorando.", eventId);
            return;
        }

        log.info("[Receiver] ====================================");
        log.info("[Receiver] Processando evento: {}", eventId);
        log.info("[Receiver] ====================================");

        processedEvents.add(eventId);
    }

}
