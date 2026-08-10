package br.com.danilodps.receiver.infrastructure.coordinator;

import br.com.danilodps.receiver.domain.SecretResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class CoordinatorClient {

    private static final String SECRET_CURRENT_URI = "/secrets/current";
    private final RestClient coordinatorRestClient;

    public CoordinatorClient(RestClient coordinatorRestClient) {
        this.coordinatorRestClient = coordinatorRestClient;
    }

    public SecretResponse fetchCurrentSecret() {
        try {
            return coordinatorRestClient.get()
                    .uri(SECRET_CURRENT_URI)
                    .retrieve()
                    .body(SecretResponse.class);
        } catch (ResourceAccessException e) {
            log.error("[Receiver] Coordinator indisponível: {}", e.getMessage());
            throw e;
        }
    }

}