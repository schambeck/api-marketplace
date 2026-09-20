package com.schambeck.marketplace.usecase.catalog;

import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.product.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {
    @Mock
    private ProductService productService;

    @Mock
    private OfferService offerService;

    @Test
    void forwardsSearchParameterToPublishedOffersService() {
        CatalogController controller = new CatalogController(productService, offerService);
        PageRequest pageable = PageRequest.of(0, 20);
        when(offerService.searchPublished(pageable, "elden ring")).thenReturn(Page.empty());

        assertThat(controller.searchOffers(pageable, "elden ring").getStatusCode().is2xxSuccessful()).isTrue();

        verify(offerService).searchPublished(pageable, "elden ring");
    }
}
