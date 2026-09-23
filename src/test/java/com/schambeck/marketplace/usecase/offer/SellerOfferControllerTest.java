package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.security.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SellerOfferControllerTest {
    private final OfferService service = mock(OfferService.class);
    private final AuthenticationService authenticationService = mock(AuthenticationService.class);
    private final SellerOfferController controller = new SellerOfferController(service, authenticationService);
    private final Principal principal = mock(Principal.class);

    @Test
    void searchesOnlyForAuthenticatedUsernameAndParsesStatus() {
        PageRequest pageable = PageRequest.of(0, 25);
        when(authenticationService.getLoggedUsername(principal)).thenReturn("seller-a");
        when(service.searchOwned("seller-a", pageable, OfferStatus.PAUSED, "elden")).thenReturn(Page.empty());

        assertThat(controller.search(pageable, "paused", "elden", principal).getStatusCode().is2xxSuccessful()).isTrue();

        verify(service).searchOwned("seller-a", pageable, OfferStatus.PAUSED, "elden");
    }

    @Test
    void rejectsUnknownStatus() {
        assertThatThrownBy(() -> controller.search(PageRequest.of(0, 25), "unknown", null, principal))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DRAFT, PUBLISHED, PAUSED or RETIRED");
        verifyNoInteractions(authenticationService, service);
    }
}
