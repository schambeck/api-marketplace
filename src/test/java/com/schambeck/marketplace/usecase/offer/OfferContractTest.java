package com.schambeck.marketplace.usecase.offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OfferContractTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sellerResponseDoesNotExposeLegacyOrFinancialFields() throws Exception {
        SellerOfferWeb web = new SellerOfferWeb();
        web.setId(11);
        web.setProductId(21);
        web.setGame("Elden Ring");
        web.setPlatform("PC");
        web.setPrice(new BigDecimal("25.00"));
        web.setDeliveryMethod("AUTOMATIC");
        web.setStatus(OfferStatus.DRAFT);

        String json = objectMapper.writeValueAsString(web);

        assertThat(json).contains("\"productId\"", "\"game\"", "\"platform\"", "\"price\"");
        assertThat(json).doesNotContain("availabilitySource", "settlement", "cost", "supply", "gameContextId", "contextVersion");
    }
}
