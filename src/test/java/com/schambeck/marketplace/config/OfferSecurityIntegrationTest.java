package com.schambeck.marketplace.config;

import com.schambeck.marketplace.security.AuthenticationService;
import com.schambeck.marketplace.usecase.offer.AdminOfferController;
import com.schambeck.marketplace.usecase.offer.Offer;
import com.schambeck.marketplace.usecase.offer.OfferService;
import com.schambeck.marketplace.usecase.offer.SellerOfferController;
import com.schambeck.marketplace.usecase.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SellerOfferController.class, AdminOfferController.class})
@Import(SecurityConfig.class)
class OfferSecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferService service;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private JwtDecoder jwtDecoder;
    @MockBean
    private AuditingHandler auditingHandler;
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void anonymousUserCannotAccessSellerOffers() throws Exception {
        mockMvc.perform(get("/seller/offers"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service, authenticationService);
    }

    @Test
    void authenticatedUserWithoutSellerOrAdminRoleCannotAccessSellerOffers() throws Exception {
        mockMvc.perform(get("/seller/offers").with(jwt()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service, authenticationService);
    }

    @Test
    void sellerCanAccessOnlySellerQuery() throws Exception {
        when(authenticationService.getLoggedUsername(any())).thenReturn("seller-a");
        when(service.searchOwned(eq("seller-a"), any(), isNull(), isNull())).thenReturn(Page.empty());

        mockMvc.perform(get("/seller/offers").with(jwt().authorities(() -> "ROLE_SELLER")))
                .andExpect(status().isOk());

        verify(service).searchOwned(eq("seller-a"), any(), isNull(), isNull());
        verifyNoMoreInteractions(service);
    }

    @Test
    void nonAdminCannotAccessAdministrativeDetail() throws Exception {
        mockMvc.perform(get("/admin/offers/11").with(jwt().authorities(() -> "ROLE_SELLER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void adminCanAccessAdministrativeDetail() throws Exception {
        when(service.findAdminById(11)).thenReturn(Offer.builder().id(11).product(Product.builder().id(21).build()).build());

        mockMvc.perform(get("/admin/offers/11").with(jwt().authorities(() -> "ROLE_ADMIN")))
                .andExpect(status().isOk());

        verify(service).findAdminById(11);
    }
}
