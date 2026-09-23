package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.GameContextValidationException;
import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.gamecontext.GameContextReference;
import com.schambeck.marketplace.usecase.gamecontext.GameContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {
    @Mock
    private OfferRepository repository;

    @Mock
    private OfferStatusHistoryRepository historyRepository;

    @Mock
    private com.schambeck.marketplace.usecase.product.ProductRepository productRepository;

    @Mock
    private GameContextService gameContextService;

    @InjectMocks
    private OfferService service;

    @Test
    void publishesOnlyWhenContextAndAvailabilityAreValid() {
        Offer offer = offer(OfferStatus.DRAFT, OfferAvailabilityStatus.AVAILABLE);
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);
        when(gameContextService.referenceForOfferPublication(11)).thenReturn(
                new GameContextReference(11, 3, "Elden Ring", "FromSoftware", "PC", "BR", null, "Standard"));
        when(repository.save(offer)).thenReturn(offer);

        Offer published = service.publish(7, action(), "admin");

        assertThat(published.getStatus()).isEqualTo(OfferStatus.PUBLISHED);
        ArgumentCaptor<OfferStatusHistory> history = ArgumentCaptor.forClass(OfferStatusHistory.class);
        verify(historyRepository).save(history.capture());
        assertThat(history.getValue().getFromStatus()).isEqualTo(OfferStatus.DRAFT);
        assertThat(history.getValue().getToStatus()).isEqualTo(OfferStatus.PUBLISHED);
        assertThat(history.getValue().getChangedBy()).isEqualTo("admin");
    }

    @Test
    void rejectsPublicationWhenContextIsNotActive() {
        Offer offer = offer(OfferStatus.DRAFT, OfferAvailabilityStatus.AVAILABLE);
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);
        when(gameContextService.referenceForOfferPublication(11))
                .thenThrow(new GameContextValidationException("context is not active"));

        assertThatThrownBy(() -> service.publish(7, action(), "admin"))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessageContaining("not active");
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsPublicationWhenAvailabilityIsUnavailable() {
        Offer offer = offer(OfferStatus.DRAFT, OfferAvailabilityStatus.UNAVAILABLE);
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);

        assertThatThrownBy(() -> service.publish(7, action(), "admin"))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("AVAILABLE");
        verifyNoInteractions(gameContextService);
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Offer offer = offer(OfferStatus.RETIRED, OfferAvailabilityStatus.AVAILABLE);
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);

        assertThatThrownBy(() -> service.pause(7, action(), "admin"))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("Invalid offer transition");
    }

    @Test
    void checkoutRevalidationBlocksPausedOffers() {
        Offer offer = offer(OfferStatus.PAUSED, OfferAvailabilityStatus.AVAILABLE);
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);

        assertThatThrownBy(() -> service.validateForCheckout(7))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("not published");
        verifyNoInteractions(gameContextService);
    }

    @Test
    void normalizesAdminSearchBeforeQueryingRepository() {
        Page<Offer> page = Page.empty();
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.searchAdmin(OfferStatus.PAUSED, "elden ring", pageable)).thenReturn(page);

        assertThat(service.searchAdmin(pageable, OfferStatus.PAUSED, "  elden ring  ")).isSameAs(page);

        verify(repository).searchAdmin(OfferStatus.PAUSED, "elden ring", pageable);
    }

    @Test
    void normalizesSearchBeforeQueryingPublishedOffers() {
        Page<Offer> page = Page.empty();
        when(repository.findAllByStatusAndSearch(OfferStatus.PUBLISHED, "elden ring", PageRequest.of(0, 10)))
                .thenReturn(page);

        assertThat(service.searchPublished(PageRequest.of(0, 10), "  elden ring  ")).isSameAs(page);

        verify(repository).findAllByStatusAndSearch(OfferStatus.PUBLISHED, "elden ring", PageRequest.of(0, 10));
    }

    @Test
    void treatsBlankSearchAsNoFilter() {
        Page<Offer> page = Page.empty();
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.findAllByStatusAndSearch(OfferStatus.PUBLISHED, "", pageable)).thenReturn(page);

        assertThat(service.searchPublished(pageable, "   ")).isSameAs(page);
        verify(repository).findAllByStatusAndSearch(OfferStatus.PUBLISHED, "", pageable);
    }

    private static Offer offer(OfferStatus status, OfferAvailabilityStatus availabilityStatus) {
        return Offer.builder()
                .id(7)
                .product(com.schambeck.marketplace.usecase.product.Product.builder().id(21).build())
                .gameContext(com.schambeck.marketplace.usecase.gamecontext.GameContext.builder().id(11).build())
                .contextVersion(2)
                .contextGame("Elden Ring")
                .contextPlatform("PC")
                .price(new BigDecimal("25.00"))
                .deliveryMethod("AUTOMATIC")
                .availabilitySource("SUPPLY")
                .availabilityStatus(availabilityStatus)
                .status(status)
                .version(0)
                .build();
    }

    private static OfferActionRequest action() {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason("catalog validation");
        request.setSource("admin-api");
        return request;
    }
}
