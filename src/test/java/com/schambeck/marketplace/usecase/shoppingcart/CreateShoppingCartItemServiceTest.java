package com.schambeck.marketplace.usecase.shoppingcart;

import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.cartitem.CreateShoppingCartItemRequest;
import com.schambeck.marketplace.usecase.offer.Offer;
import com.schambeck.marketplace.usecase.offer.OfferRepository;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.offer.OfferStatus;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShoppingCartItemServiceTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OfferRepository offerRepository;
    @Mock
    private OfferService offerService;
    @InjectMocks
    private CreateShoppingCartItemService service;

    @Test
    void cartItemKeepsOfferAndCapturedOfferPrice() {
        User buyer = User.builder().username("buyer").build();
        Product product = Product.builder().id(21).name("Digital item").build();
        Offer offer = Offer.builder().id(7).product(product).price(new BigDecimal("25.00"))
                .status(OfferStatus.PUBLISHED).build();
        Cart cart = Cart.builder().user(buyer).total(BigDecimal.ZERO).build();

        when(offerRepository.findByIdOrThrow(7)).thenReturn(offer);
        when(offerService.validateForCheckout(7)).thenReturn(offer);
        when(cartRepository.findByUser_Username("buyer")).thenReturn(Optional.of(cart));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateShoppingCartItemRequest request = new CreateShoppingCartItemRequest();
        request.setOfferId(7);
        request.setQuantity(new BigDecimal("2"));

        CartItem item = service.create(request, "buyer");

        assertThat(item.getOffer()).isSameAs(offer);
        assertThat(item.getProduct()).isSameAs(product);
        assertThat(item.getPrice()).isEqualByComparingTo("25.00");
        assertThat(item.getTotal()).isEqualByComparingTo("50.00");
        verify(offerService).validateForCheckout(7);
    }
}
