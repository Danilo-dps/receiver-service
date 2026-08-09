package br.com.danilodps.receiver.controller;

import br.com.danilodps.receiver.infrastructure.cache.DualSecretCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cache")
public class CacheAdminController {

    private final DualSecretCache secretCache;

    public CacheAdminController(DualSecretCache secretCache) {
        this.secretCache = secretCache;
    }

    @PostMapping("/invalidate")
    public ResponseEntity<String> invalidate() {
        secretCache.invalidateAll();
        return ResponseEntity.ok("Cache invalidado");
    }

}
