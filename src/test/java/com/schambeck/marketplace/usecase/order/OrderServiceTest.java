package com.schambeck.marketplace.usecase.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schambeck.marketplace.exception.OrderValidationException;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.inventory.InventoryService;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orders;
    @Mock OrderItemRepository items;
    @Mock OrderStatusHistoryRepository history;
    @Mock CartRepository carts;
    @Mock CartItemRepository cartItems;
    @Mock UserRepository users;
    @Mock OfferService offers;
    @Mock InventoryService inventory;

    private OrderService service() {
        return new OrderService(orders, items, history, carts, cartItems, users, offers, inventory, new ObjectMapper());
    }

    @Test
    void rejectsCheckoutWithoutIdempotencyKey() {
        assertThatThrownBy(() -> service().checkout("buyer", " ", null))
                .isInstanceOf(OrderValidationException.class)
                .hasMessageContaining("Idempotency-Key");
    }

    @Test
    void rejectsInvalidTransitionWithoutChangingOrder() {
        Order order = Order.builder().id(1).publicId(UUID.randomUUID()).status(OrderStatus.CREATED).build();
        when(orders.findByPublicIdForUpdate(order.getPublicId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service().transition(order.getPublicId(), OrderStatus.PAID,
                "payment", "test", "actor", "key"))
                .isInstanceOf(OrderValidationException.class)
                .hasMessageContaining("Invalid order transition");
    }
}
