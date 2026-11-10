package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private ProductRepository productRepository;
    @InjectMocks
    private OfferService service;

    @Test
    void sellerCanPublishOwnOfferWhenProductHasStock() {
        Product product = product("seller-a", new BigDecimal("10"));
        when(productRepository.findByIdOrThrow(21)).thenReturn(product);
        when(repository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Offer offer = service.create(request(), "seller-a", false);
        when(repository.findByIdForUpdateOrThrow(offer.getId())).thenReturn(offer);

        Offer published = service.publish(offer.getId(), action(), "seller-a", false);

        assertThat(published.getStatus()).isEqualTo(OfferStatus.PUBLISHED);
        verify(historyRepository, times(2)).save(any(OfferStatusHistory.class));
    }

    @Test
    void sellerCannotOperateAnotherUsersProduct() {
        Product product = product("seller-b", new BigDecimal("10"));
        when(productRepository.findByIdOrThrow(21)).thenReturn(product);

        assertThatThrownBy(() -> service.create(request(), "seller-a", false))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("does not belong");
        verify(repository, never()).save(any());
    }

    @Test
    void productWithoutStockCannotBePublished() {
        Product product = product("seller-a", BigDecimal.ZERO);
        when(productRepository.findByIdOrThrow(21)).thenReturn(product);
        when(repository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Offer offer = service.create(request(), "seller-a", false);
        when(repository.findByIdForUpdateOrThrow(offer.getId())).thenReturn(offer);

        assertThatThrownBy(() -> service.publish(offer.getId(), action(), "seller-a", false))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("available product stock");
    }

    @Test
    void sellerCanCreateOfferWithOnlyRequiredContextFields() {
        Product product = product("seller-a", new BigDecimal("10"));
        when(productRepository.findByIdOrThrow(21)).thenReturn(product);
        when(repository.save(any(Offer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OfferWeb request = request();
        request.setRegion(null);
        request.setVariant(null);

        Offer offer = service.create(request, "seller-a", false);

        assertThat(offer.getContextGame()).isEqualTo("Elden Ring");
        assertThat(offer.getContextPlatform()).isEqualTo("PC");
        assertThat(offer.getContextRegion()).isNull();
        assertThat(offer.getContextVariant()).isNull();
    }

    @Test
    void offerWithoutGameOrPlatformIsRejectedBeforePersistence() {
        OfferWeb request = request();
        request.setGame(" ");

        assertThatThrownBy(() -> service.create(request, "seller-a", false))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("game and platform are required");
        verify(repository, never()).save(any());
    }

    @Test
    void publicSearchUsesProductStock() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Offer> page = Page.empty();
        when(repository.findAllPublishedAndAvailable("elden ring", pageable)).thenReturn(page);

        assertThat(service.searchPublished(pageable, "  elden ring  ")).isSameAs(page);
        verify(repository).findAllPublishedAndAvailable("elden ring", pageable);
    }

    @Test
    void ownedSearchDelegatesUsernameAndFiltersWithoutMutatingState() {
        PageRequest pageable = PageRequest.of(0, 25);
        Page<Offer> page = Page.empty();
        when(repository.searchOwned("seller-a", OfferStatus.PAUSED, "elden", pageable)).thenReturn(page);

        assertThat(service.searchOwned("seller-a", pageable, OfferStatus.PAUSED, "  elden  ")).isSameAs(page);

        verify(repository).searchOwned("seller-a", OfferStatus.PAUSED, "elden", pageable);
        verifyNoInteractions(historyRepository, productRepository);
        verify(repository, never()).save(any());
    }

    @Test
    void ownedDetailReturnsNotFoundForMissingOrForeignOffer() {
        when(repository.findByIdAndOwnerUsername(7, "seller-a")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.findOwnedById("seller-a", 7))
                .isInstanceOf(com.schambeck.marketplace.exception.EntityNotFoundException.class);

        verify(repository).findByIdAndOwnerUsername(7, "seller-a");
        verifyNoInteractions(historyRepository, productRepository);
        verify(repository, never()).save(any());
    }

    @Test
    void checkoutBlocksPausedOffer() {
        Offer offer = Offer.builder().id(7).status(OfferStatus.PAUSED)
                .product(product("seller-a", new BigDecimal("10"))).build();
        when(repository.findByIdForUpdateOrThrow(7)).thenReturn(offer);

        assertThatThrownBy(() -> service.validateForCheckout(7))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("not published");
    }

    private OfferWeb request() {
        OfferWeb request = new OfferWeb();
        request.setProductId(21);
        request.setGame("Elden Ring");
        request.setPlatform("PC");
        request.setRegion("BR");
        request.setVariant("Standard");
        request.setPrice(new BigDecimal("25.00"));
        request.setDeliveryMethod("AUTOMATIC");
        return request;
    }

    private static Product product(String username, BigDecimal quantity) {
        return Product.builder().id(21).quantity(quantity)
                .user(User.builder().username(username).build()).build();
    }

    private static OfferActionRequest action() {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason("seller publication");
        request.setSource("seller-api");
        return request;
    }
}
