package com.schambeck.marketplace.usecase.inventory;

import com.schambeck.marketplace.exception.EntityNotFoundException;
import com.schambeck.marketplace.exception.InventoryValidationException;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.order.Order;
import com.schambeck.marketplace.usecase.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.schambeck.marketplace.usecase.inventory.InventoryStatus.*;
import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryAllocationRepository allocationRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryOperationRepository operationRepository;
    private final OrderRepository orderRepository;

    @Value("${inventory.reservation-ttl:30}")
    private long reservationTtlMinutes;

    @Value("${inventory.expiration-batch-size:100}")
    private int expirationBatchSize;

    @Transactional
    public InventoryReservation reserve(Integer productId, Integer orderItemId, BigDecimal quantity,
                                        String operationKey, String actor) {
        requireOperationKey(operationKey);
        if (orderItemId == null) throw new InventoryValidationException("orderItemId is required.");
        InventoryReservation repeated = reservationRepository.findByOperationKey(operationKey).orElse(null);
        if (repeated != null) {
            return repeated;
        }
        validateQuantity(quantity);
        InventoryBalance balance = lockBalance(productId);
        if (balance.getAvailableQuantity().compareTo(quantity) < 0) {
            throw new InventoryValidationException("Insufficient inventory for product " + productId + ".");
        }
        Product product = balance.getProduct();
        balance.setAvailableQuantity(balance.getAvailableQuantity().subtract(quantity));
        balance.setReservedQuantity(balance.getReservedQuantity().add(quantity));
        syncProductProjection(balance);
        if (product.getUser() == null) {
            throw new InventoryValidationException("Product must have a seller before reservation.");
        }
        InventoryReservation reservation = reservationRepository.save(InventoryReservation.builder()
                .orderItemId(orderItemId).product(product).seller(product.getUser()).quantity(quantity).status(RESERVED)
                .operationKey(operationKey).expiresAt(LocalDateTime.now().plusMinutes(reservationTtlMinutes)).build());
        recordOperation(reservation.getId(), null, "RESERVE", operationKey, quantity);
        record(product.getId(), reservation.getId(), null, "RESERVE", AVAILABLE, RESERVED, quantity, operationKey, actor, "checkout");
        return reservation;
    }

    @Transactional
    public InventoryAllocation allocate(Integer reservationId, BigDecimal quantity, String operationKey, String actor) {
        return allocate(reservationId, quantity, operationKey, actor, false);
    }

    @Transactional
    public InventoryAllocation allocate(Integer reservationId, BigDecimal quantity, String operationKey, String actor, boolean admin) {
        requireOperationKey(operationKey);
        InventoryReservation reservation = lockReservation(reservationId);
        requireSellerOrAdmin(reservation, actor, admin);
        InventoryAllocation repeated = allocationRepository.findByOperationKey(operationKey).orElse(null);
        if (repeated != null) return repeated;
        validateQuantity(quantity);
        ensureActive(reservation);
        if (quantity.compareTo(reservation.getQuantity()) != 0) {
            throw new InventoryValidationException("Allocation must match the reservation quantity in the MVP.");
        }
        InventoryBalance balance = lockBalance(reservation.getProduct().getId());
        balance.setReservedQuantity(balance.getReservedQuantity().subtract(quantity));
        balance.setAllocatedQuantity(balance.getAllocatedQuantity().add(quantity));
        syncProductProjection(balance);
        InventoryAllocation allocation = allocationRepository.save(InventoryAllocation.builder()
                .reservation(reservation).quantity(quantity).consumedQuantity(ZERO)
                .status(InventoryAllocationStatus.ACTIVE)
                .operationKey(operationKey).build());
        reservation.setStatus(ALLOCATED);
        recordOperation(reservationId, allocation.getId(), "ALLOCATE", operationKey, quantity);
        record(reservation.getProduct().getId(), reservationId, allocation.getId(), "ALLOCATE", RESERVED, ALLOCATED, quantity, operationKey, actor, "seller_product");
        return allocation;
    }

    @Transactional
    public InventoryReservation release(Integer reservationId, String operationKey, String actor) {
        return release(reservationId, operationKey, actor, false);
    }

    @Transactional
    public InventoryReservation release(Integer reservationId, String operationKey, String actor, boolean admin) {
        requireOperationKey(operationKey);
        InventoryReservation reservation = lockReservation(reservationId);
        requireSellerOrAdmin(reservation, actor, admin);
        if (operationKey.equals(reservation.getReleaseOperationKey())) return reservation;
        if (reservation.getReleaseOperationKey() != null) {
            throw new InventoryValidationException("Reservation was already released with another operation key.");
        }
        if (reservation.getStatus() == InventoryStatus.RELEASED || reservation.getStatus() == EXPIRED) return reservation;
        InventoryStatus previousStatus = reservation.getStatus();
        InventoryBalance balance = lockBalance(reservation.getProduct().getId());
        BigDecimal releasedQuantity = reservation.getQuantity();
        if (reservation.getStatus() == ALLOCATED) {
            InventoryAllocation allocation = allocationRepository.findActiveForUpdate(reservationId,
                            java.util.List.of(InventoryAllocationStatus.ACTIVE, InventoryAllocationStatus.PARTIALLY_CONSUMED))
                    .orElseThrow(() -> new InventoryValidationException("Active allocation not found."));
            releasedQuantity = allocation.getQuantity().subtract(allocation.getConsumedQuantity());
            balance.setAllocatedQuantity(balance.getAllocatedQuantity().subtract(releasedQuantity));
            allocation.setStatus(InventoryAllocationStatus.RELEASED);
        } else if (reservation.getStatus() == RESERVED) {
            balance.setReservedQuantity(balance.getReservedQuantity().subtract(reservation.getQuantity()));
        } else {
            throw new InventoryValidationException("Only an active inventory can be released.");
        }
        balance.setAvailableQuantity(balance.getAvailableQuantity().add(releasedQuantity));
        syncProductProjection(balance);
        reservation.setStatus(InventoryStatus.RELEASED);
        reservation.setReleaseOperationKey(operationKey);
        recordOperation(reservationId, null, "RELEASE", operationKey, releasedQuantity);
        record(reservation.getProduct().getId(), reservationId, null, "RELEASE", previousStatus, InventoryStatus.RELEASED, releasedQuantity, operationKey, actor, "operation");
        return reservation;
    }

    @Transactional
    public InventoryReservation expire(Integer reservationId, String operationKey, String actor) {
        requireOperationKey(operationKey);
        InventoryReservation reservation = lockReservation(reservationId);
        if (operationKey.equals(reservation.getExpirationOperationKey())) return reservation;
        if (reservation.getExpirationOperationKey() != null) {
            throw new InventoryValidationException("Reservation was already expired with another operation key.");
        }
        if (reservation.getStatus() == EXPIRED || reservation.getStatus() == InventoryStatus.RELEASED) return reservation;
        if (reservation.getStatus() != RESERVED || reservation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InventoryValidationException("Reservation is not eligible for expiration.");
        }
        InventoryBalance balance = lockBalance(reservation.getProduct().getId());
        balance.setReservedQuantity(balance.getReservedQuantity().subtract(reservation.getQuantity()));
        balance.setAvailableQuantity(balance.getAvailableQuantity().add(reservation.getQuantity()));
        syncProductProjection(balance);
        reservation.setStatus(EXPIRED);
        reservation.setExpirationOperationKey(operationKey);
        recordOperation(reservationId, null, "EXPIRE", operationKey, reservation.getQuantity());
        record(reservation.getProduct().getId(), reservationId, null, "EXPIRE", RESERVED, EXPIRED, reservation.getQuantity(), operationKey, actor, "scheduler");
        return reservation;
    }

    @Transactional
    public InventoryAllocation consumeDelivered(Integer allocationId, BigDecimal quantity, String operationKey, String actor) {
        return consumeDelivered(allocationId, quantity, operationKey, actor, false);
    }

    @Transactional
    public InventoryAllocation consumeDelivered(Integer allocationId, BigDecimal quantity, String operationKey, String actor, boolean admin) {
        requireOperationKey(operationKey);
        InventoryAllocation allocation = allocationRepository.findByIdForUpdate(allocationId)
                .orElseThrow(() -> new EntityNotFoundException(InventoryAllocation.class, allocationId));
        requireSellerOrAdmin(allocation.getReservation(), actor, admin);
        InventoryOperation repeated = operationRepository.findByOperationTypeAndOperationKey("CONSUME", operationKey).orElse(null);
        if (repeated != null) {
            return allocationRepository.findById(repeated.getAllocationId())
                    .orElseThrow(() -> new EntityNotFoundException(InventoryAllocation.class, repeated.getAllocationId()));
        }
        validateQuantity(quantity);
        if (allocation.getStatus() != InventoryAllocationStatus.ACTIVE
                && allocation.getStatus() != InventoryAllocationStatus.PARTIALLY_CONSUMED) {
            throw new InventoryValidationException("Only an active allocation can be consumed.");
        }
        BigDecimal remaining = allocation.getQuantity().subtract(allocation.getConsumedQuantity());
        if (remaining.compareTo(quantity) < 0) throw new InventoryValidationException("Consumption exceeds allocated quantity.");
        InventoryBalance balance = lockBalance(allocation.getReservation().getProduct().getId());
        balance.setAllocatedQuantity(balance.getAllocatedQuantity().subtract(quantity));
        balance.setConsumedQuantity(balance.getConsumedQuantity().add(quantity));
        allocation.setConsumedQuantity(allocation.getConsumedQuantity().add(quantity));
        allocation.setStatus(allocation.getConsumedQuantity().compareTo(allocation.getQuantity()) == 0
                ? InventoryAllocationStatus.CONSUMED : InventoryAllocationStatus.PARTIALLY_CONSUMED);
        if (allocation.getStatus() == InventoryAllocationStatus.CONSUMED) {
            allocation.getReservation().setStatus(InventoryStatus.CONSUMED);
        }
        recordOperation(allocation.getReservation().getId(), allocationId, "CONSUME", operationKey, quantity);
        record(balance.getProduct().getId(), allocation.getReservation().getId(), allocationId, "CONSUME", ALLOCATED, CONSUMED, quantity, operationKey, actor, "delivery");
        return allocation;
    }

    @Transactional
    public InventoryAllocation consumeReservation(Integer reservationId, BigDecimal quantity, String operationKey,
                                                   String actor, boolean admin) {
        InventoryReservation reservation = lockReservation(reservationId);
        requireSellerOrAdmin(reservation, actor, admin);
        InventoryAllocation allocation = allocationRepository.findActiveForUpdate(reservationId,
                        java.util.List.of(InventoryAllocationStatus.ACTIVE, InventoryAllocationStatus.PARTIALLY_CONSUMED))
                .orElseThrow(() -> new InventoryValidationException("Active allocation not found."));
        return consumeDelivered(allocation.getId(), quantity, operationKey, actor, admin);
    }

    private static void syncProductProjection(InventoryBalance balance) {
        balance.getProduct().setQuantity(balance.getAvailableQuantity());
    }

    @Transactional
    public int expireReservations() {
        List<InventoryReservation> expired = reservationRepository.findExpiredForUpdate(RESERVED, LocalDateTime.now())
                .stream().limit(expirationBatchSize).toList();
        expired.forEach(reservation -> expire(reservation.getId(), "expiry:" + reservation.getId(), "scheduler"));
        return expired.size();
    }

    @Transactional(readOnly = true)
    public InventoryOrderWeb orderSummary(UUID orderId, String actor, boolean admin) {
        Order order = orderRepository.findByPublicId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(Order.class, orderId));
        List<InventoryReservation> reservations = reservationRepository.findByOrderPublicId(orderId);
        if (!admin && (order.getBuyer() == null || !actor.equals(order.getBuyer().getUsername()))
                && reservations.stream().noneMatch(r -> r.getSeller() != null && actor.equals(r.getSeller().getUsername()))) {
            throw new InventoryValidationException("The order does not belong to the authenticated user.");
        }
        List<Integer> reservationIds = reservations.stream().map(InventoryReservation::getId).toList();
        InventoryOrderWeb result = new InventoryOrderWeb();
        result.setOrderId(orderId);
        result.setReservations(reservations.stream().map(InventoryReservationWeb::from).toList());
        result.setAllocations(reservationIds.isEmpty() ? List.of() : allocationRepository.findAllByReservationIdIn(reservationIds)
                .stream().map(InventoryAllocationWeb::from).toList());
        result.setMovements(reservationIds.isEmpty() ? List.of() : movementRepository.findAllByReservationIdInOrderByIdAsc(reservationIds)
                .stream().map(InventoryMovementWeb::from).toList());
        return result;
    }

    private InventoryBalance lockBalance(Integer productId) {
        return balanceRepository.findByProductIdForUpdate(productId).orElseGet(() -> {
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new EntityNotFoundException(Product.class, productId));
            return balanceRepository.findByProductIdForUpdate(productId).orElseGet(() ->
                    balanceRepository.saveAndFlush(InventoryBalance.builder()
                            .product(product)
                            .availableQuantity(product.getQuantity() == null ? ZERO : product.getQuantity())
                            .reservedQuantity(ZERO)
                            .allocatedQuantity(ZERO)
                            .consumedQuantity(ZERO)
                            .version(0)
                            .build()));
        });
    }

    private InventoryReservation lockReservation(Integer id) {
        return reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException(InventoryReservation.class, id));
    }

    private void ensureActive(InventoryReservation reservation) {
        if (reservation.getStatus() != RESERVED || reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InventoryValidationException("Reservation is not active.");
        }
    }

    private void record(Integer productId, Integer reservationId, Integer allocationId, String operation,
                        InventoryStatus from, InventoryStatus to, BigDecimal quantity, String key, String actor, String source) {
        movementRepository.save(InventoryMovement.builder().productId(productId).reservationId(reservationId)
                .allocationId(allocationId).operation(operation).fromStatus(from.name()).toStatus(to.name())
                .quantity(quantity).operationKey(key).actor(actor).source(source).createdAt(LocalDateTime.now()).build());
    }

    private void recordOperation(Integer reservationId, Integer allocationId, String operationType,
                                 String operationKey, BigDecimal quantity) {
        operationRepository.save(InventoryOperation.builder().reservationId(reservationId)
                .allocationId(allocationId).operationType(operationType).operationKey(operationKey)
                .quantity(quantity).createdAt(LocalDateTime.now()).build());
    }

    private static void requireOperationKey(String operationKey) {
        if (operationKey == null || operationKey.isBlank()) throw new InventoryValidationException("operationKey is required.");
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0 || quantity.scale() > 2) {
            throw new InventoryValidationException("quantity must be positive with at most two decimal places.");
        }
    }

    private static void requireSellerOrAdmin(InventoryReservation reservation, String actor, boolean admin) {
        if (!admin && (actor == null || reservation.getSeller() == null
                || !actor.equals(reservation.getSeller().getUsername()))) {
            throw new InventoryValidationException("Only the seller or an administrator can operate this reservation.");
        }
    }
}
