package com.schambeck.marketplace.usecase.offer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOfferControllerTest {
    @Mock
    private OfferService service;

    @Test
    void parsesStatusAndForwardsAdminSearch() {
        AdminOfferController controller = new AdminOfferController(service);
        PageRequest pageable = PageRequest.of(0, 25);
        when(service.searchAdmin(pageable, OfferStatus.PAUSED, "elden")).thenReturn(Page.empty());

        assertThat(controller.search(pageable, "paused", "elden").getStatusCode().is2xxSuccessful()).isTrue();

        verify(service).searchAdmin(pageable, OfferStatus.PAUSED, "elden");
    }

    @Test
    void rejectsUnknownStatusWithClearMessage() {
        AdminOfferController controller = new AdminOfferController(service);

        assertThatThrownBy(() -> controller.search(PageRequest.of(0, 25), "unknown", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT, PUBLISHED, PAUSED or RETIRED");
    }
}
