package com.schambeck.marketplace.usecase.offer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.schambeck.marketplace.usecase.product.Product;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOfferControllerTest {
    @Mock
    private OfferService service;

    @Test
    void returnsAdministrativeDetail() {
        AdminOfferController controller = new AdminOfferController(service);
        Offer offer = Offer.builder().id(11).product(Product.builder().id(21).build()).build();
        when(service.findAdminById(11)).thenReturn(offer);

        assertThat(controller.get(11).getStatusCode().is2xxSuccessful()).isTrue();
    }
}
