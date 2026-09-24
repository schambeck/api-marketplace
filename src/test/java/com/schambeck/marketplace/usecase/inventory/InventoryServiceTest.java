package com.schambeck.marketplace.usecase.inventory;

import com.schambeck.marketplace.exception.InventoryValidationException;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock private ProductRepository productRepository;
    @Mock private InventoryBalanceRepository balanceRepository;
    @Mock private InventoryReservationRepository reservationRepository;
    @Mock private InventoryAllocationRepository allocationRepository;
    @Mock private InventoryMovementRepository movementRepository;
    @Mock private InventoryOperationRepository operationRepository;
    @InjectMocks private InventoryService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "reservationTtlMinutes", 30L);
        ReflectionTestUtils.setField(service, "expirationBatchSize", 100);
    }

    @Test
    void reservesAtomicallyAgainstAvailableBalance() {
        Product product = Product.builder().id(1).quantity(new BigDecimal("10.00"))
                .user(User.builder().id(9).username("seller").build()).build();
        InventoryBalance balance = balance(product, "10.00");
        when(balanceRepository.findByProductIdForUpdate(1)).thenReturn(Optional.of(balance));
        when(reservationRepository.findByOperationKey("reserve-1")).thenReturn(Optional.empty());
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            InventoryReservation reservation = invocation.getArgument(0);
            reservation.setId(11);
            return reservation;
        });

        InventoryReservation reservation = service.reserve(1, 22, new BigDecimal("3.00"), "reserve-1", "seller");

        assertThat(reservation.getStatus()).isEqualTo(InventoryStatus.RESERVED);
        assertThat(balance.getAvailableQuantity()).isEqualByComparingTo("7.00");
        assertThat(balance.getReservedQuantity()).isEqualByComparingTo("3.00");
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void rejectsReservationWhenBalanceIsInsufficient() {
        Product product = Product.builder().id(1).quantity(new BigDecimal("2.00")).build();
        when(balanceRepository.findByProductIdForUpdate(1)).thenReturn(Optional.of(balance(product, "2.00")));
        when(reservationRepository.findByOperationKey("reserve-2")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserve(1, 22, new BigDecimal("3.00"), "reserve-2", "seller"))
                .isInstanceOf(InventoryValidationException.class)
                .hasMessageContaining("Insufficient inventory");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void repeatsReservationWithoutChangingTheBalance() {
        InventoryReservation existing = InventoryReservation.builder().id(11).status(InventoryStatus.RESERVED)
                .operationKey("reserve-3").build();
        when(reservationRepository.findByOperationKey("reserve-3")).thenReturn(Optional.of(existing));

        assertThat(service.reserve(1, 22, new BigDecimal("3.00"), "reserve-3", "seller")).isSameAs(existing);
        verifyNoInteractions(balanceRepository, movementRepository);
    }

    @Test
    void requiresOrderItemReference() {
        assertThatThrownBy(() -> service.reserve(1, null, new BigDecimal("1.00"), "reserve-null-item", "seller"))
                .isInstanceOf(InventoryValidationException.class)
                .hasMessageContaining("orderItemId is required");
        verifyNoInteractions(balanceRepository, reservationRepository, movementRepository);
    }

    @Test
    void rejectsConsumptionFromReleasedAllocation() {
        InventoryReservation reservation = reservation("seller");
        InventoryAllocation allocation = InventoryAllocation.builder()
                .id(30).reservation(reservation).quantity(new BigDecimal("2.00"))
                .consumedQuantity(BigDecimal.ZERO).status(InventoryAllocationStatus.RELEASED)
                .operationKey("allocate-30").build();
        when(allocationRepository.findByIdForUpdate(30)).thenReturn(Optional.of(allocation));
        when(operationRepository.findByOperationTypeAndOperationKey("CONSUME", "consume-30"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consumeDelivered(30, new BigDecimal("1.00"), "consume-30", "seller"))
                .isInstanceOf(InventoryValidationException.class)
                .hasMessageContaining("active allocation");
        verifyNoInteractions(balanceRepository);
    }

    @Test
    void repeatsConsumptionFromStoredOperationWithoutChangingBalance() {
        InventoryReservation reservation = reservation("seller");
        InventoryAllocation allocation = InventoryAllocation.builder()
                .id(30).reservation(reservation).quantity(new BigDecimal("2.00"))
                .consumedQuantity(new BigDecimal("1.00")).status(InventoryAllocationStatus.PARTIALLY_CONSUMED)
                .operationKey("allocate-30").build();
        InventoryOperation operation = InventoryOperation.builder().allocationId(30)
                .reservationId(11).operationType("CONSUME").operationKey("consume-30")
                .quantity(new BigDecimal("1.00")).build();
        when(allocationRepository.findByIdForUpdate(30)).thenReturn(Optional.of(allocation));
        when(operationRepository.findByOperationTypeAndOperationKey("CONSUME", "consume-30"))
                .thenReturn(Optional.of(operation));
        when(allocationRepository.findById(30)).thenReturn(Optional.of(allocation));

        assertThat(service.consumeDelivered(30, new BigDecimal("1.00"), "consume-30", "seller"))
                .isSameAs(allocation);
        assertThat(allocation.getConsumedQuantity()).isEqualByComparingTo("1.00");
        verifyNoInteractions(balanceRepository, movementRepository);
    }

    private static InventoryReservation reservation(String seller) {
        return InventoryReservation.builder().id(11).seller(User.builder().username(seller).build())
                .product(Product.builder().id(1).build()).status(InventoryStatus.ALLOCATED)
                .quantity(new BigDecimal("2.00")).build();
    }

    private static InventoryBalance balance(Product product, String available) {
        return InventoryBalance.builder().product(product)
                .availableQuantity(new BigDecimal(available))
                .reservedQuantity(BigDecimal.ZERO).allocatedQuantity(BigDecimal.ZERO)
                .consumedQuantity(BigDecimal.ZERO).version(0).build();
    }
}
