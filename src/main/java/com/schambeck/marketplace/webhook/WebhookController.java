package com.schambeck.marketplace.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
