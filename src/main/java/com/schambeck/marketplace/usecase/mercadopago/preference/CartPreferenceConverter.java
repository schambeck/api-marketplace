package com.schambeck.marketplace.usecase.mercadopago.preference;

import com.mercadopago.client.preference.PreferenceItemRequest;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class CartPreferenceConverter {
    private static final String BRL = "BRL";
    private final HttpServletRequest request;
    
    List<PreferenceItemRequest> createPreferenceItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::createPreferenceItem)
                .collect(Collectors.toList());
    }
    
    private PreferenceItemRequest createPreferenceItem(CartItem cartItem) {
        return PreferenceItemRequest.builder()
                .id(cartItem.getId().toString())
                .title(cartItem.getProduct().getName())
                .description(cartItem.getProduct().getDescription())
                .pictureUrl(getPictureUrl(cartItem.getProduct().getObjectId()))
                .categoryId(cartItem.getProduct().getCategory())
                .quantity(cartItem.getQuantity().intValue())
                .currencyId(BRL)
                .unitPrice(cartItem.getPrice())
                .build();
    }
    
    private String getPictureUrl(String objectId) {
        return UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(request.getRemoteHost())
                .port(request.getServerPort())
                .path("/products")
                .path("/download")
                .path("/{objectId}")
                .buildAndExpand(objectId)
                .toUriString();
    }
}
