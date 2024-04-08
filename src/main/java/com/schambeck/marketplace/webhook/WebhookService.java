package com.schambeck.marketplace.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebhookService {
    private final WebhookRepository repository;

    @Transactional
    public Webhook create(Webhook entity) {
        return repository.save(entity);
    }
}
