package com.schambeck.marketplace.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private static final WebhookMapper mapper = WebhookMapper.INSTANCE;
    private final WebhookService service;
    
    @PostMapping
    public ResponseEntity<WebhookWeb> create(@RequestBody WebhookWeb web) {
        Webhook created = service.create(mapper.toEntity(web));
        return ResponseEntity.ok(mapper.toWeb(created));
    }
    
    @PostMapping("location")
    public ResponseEntity<Void> location(@RequestBody Map<String, Object> payload) {
        log.info("Received location payload: {}", payload);
        return ResponseEntity.noContent().build();
    }
}
