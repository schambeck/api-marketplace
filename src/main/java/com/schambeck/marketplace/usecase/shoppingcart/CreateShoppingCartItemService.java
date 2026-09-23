package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.cartitem.CreateShoppingCartItemRequest;
import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.offer.Offer;
import com.schambeck.marketplace.usecase.offer.OfferRepository;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.offer.OfferStatus;
import com.schambeck.marketplace.usecase.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreateShoppingCartItemService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final OfferService offerService;

    @Transactional
    public CartItem create(CreateShoppingCartItemRequest request, String username) {
        validateQuantity(request);
        Offer offer = offerService.validateForCheckout(resolveOffer(request).getId());
        BigDecimal total = calculateTotal(offer.getPrice(), request.getQuantity());
        Cart cart = createCart(username);
        cart.setTotal(cart.getTotal().add(total));
        CartItem cartItem = createCartItem(request, cart, offer, total);
        return cartItemRepository.save(cartItem);
    }
    
    @Transactional
    public Optional<CartItem> createIfAbsent(CreateShoppingCartItemRequest request, String username) {
        Offer offer = resolveOffer(request);
        if (cartItemRepository.existsByCart_User_UsernameAndOffer_Id(username, offer.getId())) {
            return Optional.empty();
        } else {
            request.setOfferId(offer.getId());
            return Optional.of(create(request, username));
        }
    }
    
    private Offer resolveOffer(CreateShoppingCartItemRequest request) {
        if (request == null || (request.getOfferId() == null && request.getProductId() == null)) {
            throw new OfferValidationException("offerId is required; productId is accepted only for compatibility.");
        }
        if (request.getOfferId() != null) {
            return offerRepository.findByIdOrThrow(request.getOfferId());
        }
        return offerRepository.findFirstByProduct_IdAndStatusOrderByIdAsc(
                        request.getProductId(), OfferStatus.PUBLISHED)
                .orElseThrow(() -> new OfferValidationException("No published available offer exists for product " + request.getProductId() + "."));
    }

    private static CartItem createCartItem(CreateShoppingCartItemRequest request, Cart cart, Offer offer, BigDecimal total) {
        return CartItem.builder()
                .cart(cart)
                .product(offer.getProduct())
                .offer(offer)
                .price(offer.getPrice())
                .quantity(request.getQuantity())
                .total(total)
                .version(0)
                .build();
    }
    
    private Cart createCart(String username) {
        return cartRepository.findByUser_Username(username)
                .orElse(Cart.builder()
                        .user(userRepository.findByUsernameOrThrow(username))
                        .total(ZERO)
                        .version(0)
                        .build());
    }
    
    private BigDecimal calculateTotal(BigDecimal price, BigDecimal quantity) {
        return price.multiply(quantity).setScale(2, HALF_UP);
    }

    private static void validateQuantity(CreateShoppingCartItemRequest request) {
        if (request == null || request.getQuantity() == null || request.getQuantity().signum() <= 0) {
            throw new OfferValidationException("quantity must be positive.");
        }
    }
}
