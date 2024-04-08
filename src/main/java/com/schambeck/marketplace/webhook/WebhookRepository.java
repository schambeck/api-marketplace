package com.schambeck.marketplace.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookRepository extends JpaRepository<Webhook, Integer> {
}
