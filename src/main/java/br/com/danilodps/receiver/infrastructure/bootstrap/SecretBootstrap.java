package br.com.danilodps.receiver.infrastructure.bootstrap;

import br.com.danilodps.receiver.infrastructure.cache.DualSecretCache;
import br.com.danilodps.receiver.infrastructure.coordinator.CoordinatorClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Component
public class SecretBootstrap {

    private final DualSecretCache secretCache;
    private final CoordinatorClient coordinatorClient;

    public SecretBootstrap(DualSecretCache secretCache,
                           CoordinatorClient coordinatorClient) {
        this.secretCache = secretCache;
        this.coordinatorClient = coordinatorClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 3000, multiplier = 2)
    )
    public void warmCacheOnStartup() {
        log.info("[Receiver][Bootstrap] Buscando segredo no Coordinator...");
        var secret = coordinatorClient.fetchCurrentSecret();

        // warm() seta apenas o current. previous continua null no startup.
        secretCache.warm(secret);

        log.info("[Receiver][Bootstrap] OK. Versão {}", secret.version());
    }

}