package com.schambeck.marketplace.usecase.mercadopago.preference;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class CreatePreferenceService {
    private static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartPreferenceConverter preferenceConverter;
    
    @Value("${mercado-pago.access-token}")
    private String accessToken;
    @Value("${mercado-pago.feedback-url}")
    private String feedbackUrl;
    
    Preference createPreference(String username) {
        Cart cart = cartRepository.findByUser_UsernameOrThrow(username);
        List<CartItem> items = cartItemRepository.findAllByCart_IdOrderByIdDesc(cart.getId());
        List<PreferenceItemRequest> itemsRequest = preferenceConverter.createPreferenceItems(items);
        MercadoPagoConfig.setAccessToken(accessToken);
        try {
            return new PreferenceClient().create(createPreferenceRequest(cart.getId(), itemsRequest), createRequestOptions());
        } catch (MPException | MPApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static MPRequestOptions createRequestOptions() {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put(X_IDEMPOTENCY_KEY, UUID.randomUUID().toString());
        return MPRequestOptions.builder()
                .customHeaders(customHeaders)
                .build();
    }
    
    private PreferenceRequest createPreferenceRequest(Integer cartId, List<PreferenceItemRequest> items) {
        return PreferenceRequest.builder()
                .items(items)
                .externalReference(cartId.toString())
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(feedbackUrl)
                        .failure(feedbackUrl)
                        .pending(feedbackUrl)
                        .build())
                .build();
    }
}
