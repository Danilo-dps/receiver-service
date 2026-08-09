package br.com.danilodps.receiver.infrastructure.cache;

import br.com.danilodps.receiver.domain.SecretResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class DualSecretCache {

    private final AtomicReference<SecretResponse> current = new AtomicReference<>();
    private final AtomicReference<SecretResponse> previous = new AtomicReference<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public void warm(SecretResponse secret) {
        current.set(secret);
        previous.set(null);
        initialized.set(true);
        log.info("[Receiver][Cache] Warmed current with {}", secret.version());
    }

    /**
     * Promove current → previous e define novo como current.
     * Chamado pelo scheduler quando detecta uma nova versão.
     */
    public void rotate(SecretResponse novo) {
        SecretResponse antigo = current.getAndSet(novo);
        previous.set(antigo);

        if (antigo != null) {
            log.info("[Receiver][Cache] Rotated: {} → {} (previous: {})",
                    antigo.version(), novo.version(), antigo.version());
        } else {
            log.info("[Receiver][Cache] Set current: {}", novo.version());
        }
    }

    public SecretResponse findForValidation(String version) {
        SecretResponse c = current.get();
        if (c != null && c.version().equals(version)) {
            return c;
        }

        SecretResponse p = previous.get();
        if (p != null && p.version().equals(version)) {
            log.debug("[Receiver][Cache] Validating with previous version: {}", version);
            return p;
        }

        return null;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public void invalidateAll() {
        current.set(null);
        previous.set(null);
        initialized.set(false);
        log.info("[Receiver][Cache] Invalidated all");
    }

}