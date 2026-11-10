package com.schambeck.marketplace.usecase.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schambeck.marketplace.exception.EntityNotFoundException;
import com.schambeck.marketplace.exception.OrderValidationException;
import com.schambeck.marketplace.usecase.cart.Cart;
import com.schambeck.marketplace.usecase.cart.CartRepository;
import com.schambeck.marketplace.usecase.cartitem.CartItem;
import com.schambeck.marketplace.usecase.cartitem.CartItemRepository;
import com.schambeck.marketplace.usecase.inventory.InventoryService;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final OrderStatusHistoryRepository history;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final UserRepository users;
    private final OfferService offers;
    private final InventoryService inventory;
    private final ObjectMapper mapper;

    @Transactional
    public Order checkout(String username, String key, String instructions) {
        if (key == null || key.isBlank()) {
            throw new OrderValidationException("Idempotency-Key is required.");
        }
        User buyer = users.findByUsernameForCheckout(username)
                .orElseThrow(() -> new EntityNotFoundException(User.class, username));
        String instructionSnapshot = instructions == null ? null : instructionSnapshot(instructions);
        var existing = orders.findByBuyer_UsernameAndCheckoutKey(username, key);
        if (existing.isPresent()) {
            if (!Objects.equals(existing.get().getInstructions(), instructionSnapshot)) {
                throw new OrderValidationException("Idempotency-Key was already used with a different checkout payload.");
            }
            return existing.get();
        }

        Cart cart = carts.findByUser_UsernameOrThrow(username);
        List<CartItem> source = cartItems.findAllByCart_IdOrderByIdDesc(cart.getId());
        if (source.isEmpty()) {
            throw new OrderValidationException("Cart is empty.");
        }
        Order order = orders.save(Order.builder()
                .publicId(UUID.randomUUID()).buyer(buyer).cartId(cart.getId()).checkoutKey(key)
                .status(OrderStatus.CREATED).subtotalAmount(BigDecimal.ZERO)
                .discountTotal(BigDecimal.ZERO).taxTotal(BigDecimal.ZERO).shippingTotal(BigDecimal.ZERO)
                .feeTotal(BigDecimal.ZERO).totalAmount(BigDecimal.ZERO).buyerSnapshot(buyerSnapshot(buyer))
                .instructions(instructionSnapshot).build());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : source) {
            if (cartItem.getOffer() == null) {
                throw new OrderValidationException("Cart item has no offer.");
            }
            offers.validateForCheckout(cartItem.getOffer().getId());
            if (cartItem.getQuantity() == null || cartItem.getQuantity().signum() <= 0
                    || cartItem.getPrice() == null || cartItem.getPrice().signum() < 0) {
                throw new OrderValidationException("Cart item has an invalid quantity or price.");
            }
            BigDecimal subtotal = cartItem.getPrice().multiply(cartItem.getQuantity());
            total = total.add(subtotal);
            OrderItem item = items.save(OrderItem.builder()
                    .order(order).product(cartItem.getProduct()).offer(cartItem.getOffer())
                    .seller(cartItem.getProduct().getUser()).productName(cartItem.getProduct().getName())
                    .productDescription(cartItem.getProduct().getDescription())
                    .contextGame(cartItem.getOffer().getContextGame()).contextPlatform(cartItem.getOffer().getContextPlatform())
                    .contextRegion(cartItem.getOffer().getContextRegion()).contextVariant(cartItem.getOffer().getContextVariant())
                    .deliveryMethod(cartItem.getOffer().getDeliveryMethod()).unitPrice(cartItem.getPrice())
                    .quantity(cartItem.getQuantity()).subtotalAmount(subtotal).snapshotVersion(1)
                    .snapshot(itemSnapshot(cartItem)).build());
            inventory.reserve(cartItem.getProduct().getId(), item.getId(), cartItem.getQuantity(),
                    "order:" + order.getPublicId() + ":item:" + item.getId(), username);
        }
        order.setSubtotalAmount(total);
        order.setTotalAmount(total);
        history.save(OrderStatusHistory.builder().order(order).toStatus(OrderStatus.CREATED.name())
                .cause("checkout_created").source("checkout").actor(username).createdAt(LocalDateTime.now()).build());
        return order;
    }

    public List<OrderItem> items(Order order) {
        return items.findAllByOrder_IdOrderByIdAsc(order.getId());
    }

    public List<OrderStatusHistory> history(Order order) {
        return history.findAllByOrder_IdOrderByCreatedAtAsc(order.getId());
    }

    private String buyerSnapshot(User buyer) {
        ObjectNode node = mapper.createObjectNode();
        node.put("username", buyer.getUsername());
        node.put("fullName", buyer.getFullName());
        node.put("email", buyer.getEmail());
        return node.toString();
    }

    private String instructionSnapshot(String instructions) {
        ObjectNode node = mapper.createObjectNode();
        node.put("text", instructions);
        return node.toString();
    }

    private String itemSnapshot(CartItem item) {
        ObjectNode node = mapper.createObjectNode();
        node.put("productId", item.getProduct().getId());
        node.put("offerId", item.getOffer().getId());
        node.put("productName", item.getProduct().getName());
        node.put("description", item.getProduct().getDescription());
        node.put("game", item.getOffer().getContextGame());
        node.put("platform", item.getOffer().getContextPlatform());
        node.put("region", item.getOffer().getContextRegion());
        node.put("variant", item.getOffer().getContextVariant());
        node.put("deliveryMethod", item.getOffer().getDeliveryMethod());
        node.put("unitPrice", item.getPrice());
        node.put("quantity", item.getQuantity());
        return node.toString();
    }

    public Order find(UUID id, String username) {
        Order order = orders.findByPublicId(id).orElseThrow(() -> new EntityNotFoundException(Order.class, id));
        if (!order.getBuyer().getUsername().equals(username)) {
            throw new OrderValidationException("Order does not belong to the authenticated buyer.");
        }
        return order;
    }

    public Page<Order> list(String username, Pageable pageable) {
        return orders.findAllByBuyer_UsernameOrderByCreatedAtDesc(username, pageable);
    }

    @Transactional
    public Order transition(UUID id, OrderStatus target, String cause, String source, String actor, String key) {
        if (target == null || cause == null || cause.isBlank() || source == null || source.isBlank()
                || actor == null || actor.isBlank()) {
            throw new OrderValidationException("Every transition requires target, cause, source and actor.");
        }
        Order order = orders.findByPublicIdForUpdate(id).orElseThrow(() -> new EntityNotFoundException(Order.class, id));
        if (key != null && history.findByOrder_IdAndIdempotencyKey(order.getId(), key).isPresent()) {
            return order;
        }
        OrderStatus previous = order.getStatus();
        if (!allowed(previous, target)) {
            throw new OrderValidationException("Invalid order transition from " + previous + " to " + target + ".");
        }
        order.setStatus(target);
        history.save(OrderStatusHistory.builder().order(order).fromStatus(previous.name()).toStatus(target.name())
                .cause(cause).source(source).actor(actor).idempotencyKey(key).createdAt(LocalDateTime.now()).build());
        return order;
    }

    private boolean allowed(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case CREATED -> to == OrderStatus.PAYMENT_PENDING || to == OrderStatus.CANCELLED;
            case PAYMENT_PENDING -> to == OrderStatus.PAID || to == OrderStatus.REJECTED
                    || to == OrderStatus.EXPIRED || to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.FULFILLING || to == OrderStatus.REFUNDED || to == OrderStatus.DISPUTED;
            case FULFILLING -> to == OrderStatus.DELIVERED || to == OrderStatus.PARTIALLY_DELIVERED
                    || to == OrderStatus.DISPUTED;
            case PARTIALLY_DELIVERED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.COMPLETED || to == OrderStatus.REFUNDED || to == OrderStatus.DISPUTED;
            case COMPLETED -> to == OrderStatus.REFUNDED || to == OrderStatus.DISPUTED;
            default -> false;
        };
    }
}
