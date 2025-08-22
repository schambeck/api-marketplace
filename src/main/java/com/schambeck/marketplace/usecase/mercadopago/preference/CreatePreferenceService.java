package com.schambeck.marketplace.usecase.mercadopago.preference;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
class CreatePreferenceService {
    private static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartPreferenceConverter preferenceConverter;
    
//    @Value("${mercado-pago.access-token}")
//    private String accessToken;
    @Value("${mercado-pago.feedback-url}")
    private String feedbackUrl;
    
//    @PostConstruct
//    public void init() {
//        MercadoPagoConfig.setAccessToken(accessToken);
//    }
    
    Preference createPreference(String username) {
//        if (AccessTokenHolder.getAccessToken() == null) {
//            log.info("Using access token from configuration: {}", accessToken);
//            MercadoPagoConfig.setAccessToken(accessToken);
//        } else {
//            log.info("Using access token from AccessTokenHolder: {}", AccessTokenHolder.getAccessToken());
//            MercadoPagoConfig.setAccessToken(AccessTokenHolder.getAccessToken());
//        }
        Cart cart = cartRepository.findByUser_UsernameOrThrow(username);
        Optional.ofNullable(cart.getUser().getPublicKey()).orElseThrow(() -> new RuntimeException("Public Key for user " + cart.getUser().getUsername() + " not found"));
        List<CartItem> items = cartItemRepository.findAllByCart_IdOrderByIdDesc(cart.getId());
        List<PreferenceItemRequest> itemsRequest = preferenceConverter.createPreferenceItems(items);
        try {
            PreferenceRequest preferenceRequest = createPreferenceRequest(cart.getId(), itemsRequest);
            return new PreferenceClient().create(preferenceRequest, createRequestOptions());
        } catch (MPException | MPApiException e) {
            log.error("Failed to create preference for cart {}", cart.getId(), e);
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
                .marketplaceFee(new BigDecimal("0.03"))
                .autoReturn("all")
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(feedbackUrl)
                        .failure(feedbackUrl)
                        .pending(feedbackUrl)
                        .build())
                .build();
    }
}
