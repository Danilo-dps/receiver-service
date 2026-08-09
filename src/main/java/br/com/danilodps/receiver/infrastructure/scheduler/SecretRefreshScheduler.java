package br.com.danilodps.receiver.infrastructure.scheduler;

import br.com.danilodps.receiver.infrastructure.cache.DualSecretCache;
import br.com.danilodps.receiver.infrastructure.coordinator.CoordinatorClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecretRefreshScheduler {

    private final DualSecretCache secretCache;
    private final CoordinatorClient coordinatorClient;

    public SecretRefreshScheduler(DualSecretCache secretCache, CoordinatorClient coordinatorClient) {
        this.secretCache = secretCache;
        this.coordinatorClient = coordinatorClient;
    }

    @Scheduled(fixedRate = 120_000, initialDelay = 120_000)
    public void refresh() {
        // Defesa 1: não faz nada se o bootstrap ainda não aqueceu o cache
        if (!secretCache.isInitialized()) {
            log.warn("[Receiver][Scheduler] Cache ainda não inicializado pelo bootstrap. Pulando refresh.");
            return;
        }

        try {
            var remote = coordinatorClient.fetchCurrentSecret();
            var cachedCurrent = secretCache.findForValidation(remote.version());

            // Se a versão remota não é nem current nem previous, é uma rotação
            if (cachedCurrent == null) {
                log.info("[Receiver][Scheduler] Nova versão detectada: {}", remote.version());
                secretCache.rotate(remote);
            } else {
                log.info("[Receiver][Scheduler] Versão ainda atual: {}", remote.version());
            }
        } catch (Exception e) {
            log.error("[Receiver][Scheduler] Falha ao refreshar segredo: {}", e.getMessage());
        }
    }

}