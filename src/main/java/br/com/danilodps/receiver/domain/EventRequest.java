package br.com.danilodps.receiver.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record EventRequest (String eventId,
                           String orderId,
                           String status,
                           BigDecimal price,
                           Instant timestamp) {
}
