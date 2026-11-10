package com.schambeck.marketplace.usecase.mercadopago.preference;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.schambeck.marketplace.usecase.order.Order;
import com.schambeck.marketplace.usecase.order.OrderService;
import com.schambeck.marketplace.usecase.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class CreatePreferenceService {
    private static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    private final OrderService orderService;
    private final CartPreferenceConverter preferenceConverter;

    @Value("${mercado-pago.feedback-url}")
    private String feedbackUrl;

    Preference createPreference(String username, String idempotencyKey) {
        return createPreference(orderService.checkout(username, idempotencyKey, null), username, idempotencyKey);
    }

    Preference createPreferenceForOrder(UUID publicId, String username, String idempotencyKey) {
        return createPreference(orderService.find(publicId, username), username, idempotencyKey);
    }

    private Preference createPreference(Order order, String username, String idempotencyKey) {
        try {
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(preferenceConverter.createOrderPreferenceItems(orderService.items(order)))
                    .externalReference(order.getPublicId().toString())
                    .marketplaceFee(new java.math.BigDecimal("0.03"))
                    .autoReturn("all")
                    .backUrls(PreferenceBackUrlsRequest.builder().success(feedbackUrl).failure(feedbackUrl)
                            .pending(feedbackUrl).build()).build();
            Preference preference = new PreferenceClient().create(request, requestOptions(idempotencyKey));
            if (order.getStatus() == OrderStatus.CREATED) {
                orderService.transition(order.getPublicId(), OrderStatus.PAYMENT_PENDING,
                        "preference_created", "mercado_pago", username, idempotencyKey);
            }
            return preference;
        } catch (MPException | MPApiException exception) {
            log.error("Failed to create preference for order {}", order.getPublicId(), exception);
            throw new RuntimeException(exception);
        }
    }

    private MPRequestOptions requestOptions(String key) {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_IDEMPOTENCY_KEY, key == null ? UUID.randomUUID().toString() : key);
        return MPRequestOptions.builder().customHeaders(headers).build();
    }
}
