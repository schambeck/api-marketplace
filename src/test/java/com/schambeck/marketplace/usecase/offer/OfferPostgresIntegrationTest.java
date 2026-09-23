package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.user.User;
import com.schambeck.marketplace.usecase.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OfferService.class)
@Testcontainers
class OfferPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.3-alpine")
            .withDatabaseName("marketplace")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private OfferService service;
    @Autowired
    private OfferStatusHistoryRepository historyRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.connect-retries", () -> 1);
    }

    @Test
    void migratesAndPublishesSellerOwnedOffer() {
        User seller = userRepository.findByIdOrThrow(1);
        Product product = product("Game coins", seller, new BigDecimal("10"));
        Offer offer = service.create(request(product.getId()), seller.getUsername(), false);

        service.publish(offer.getId(), action("initial publication"), seller.getUsername(), false);

        assertThat(service.findPublishedById(offer.getId()).getStatus()).isEqualTo(OfferStatus.PUBLISHED);
        assertThat(historyRepository.findAllByOfferIdOrderByIdAsc(offer.getId()))
                .extracting(OfferStatusHistory::getToStatus)
                .containsExactly(OfferStatus.DRAFT, OfferStatus.PUBLISHED);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '14'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'cart_item' and column_name = 'offer_id'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    void blocksSellerFromUsingAnotherUsersProduct() {
        User owner = userRepository.findByIdOrThrow(1);
        User other = userRepository.findByIdOrThrow(2);
        Product product = product("Owned product", owner, new BigDecimal("5"));

        assertThatThrownBy(() -> service.create(request(product.getId()), other.getUsername(), false))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void publicCatalogExcludesOfferWhenProductStockReachesZero() {
        User seller = userRepository.findByIdOrThrow(1);
        Product product = product("Stocked product", seller, new BigDecimal("5"));
        Offer offer = service.create(request(product.getId()), seller.getUsername(), false);
        service.publish(offer.getId(), action("publication"), seller.getUsername(), false);

        Page<Offer> visible = service.searchPublished(PageRequest.of(0, 20), "Stocked product");
        assertThat(visible.getContent()).extracting(Offer::getId).contains(offer.getId());

        product.setQuantity(BigDecimal.ZERO);
        productRepository.save(product);

        assertThat(service.searchPublished(PageRequest.of(0, 20), "Stocked product")).isEmpty();
        assertThatThrownBy(() -> service.findPublishedById(offer.getId()))
                .isInstanceOf(com.schambeck.marketplace.exception.EntityNotFoundException.class);
    }

    private Product product(String name, User user, BigDecimal quantity) {
        return productRepository.save(Product.builder()
                .name(name).description(name).price(new BigDecimal("15.00"))
                .quantity(quantity).user(user).category("Digital").inventoryStatus("INSTOCK")
                .rating("5").image(name + ".jpg").objectId(name).version(0).build());
    }

    private static OfferWeb request(Integer productId) {
        OfferWeb request = new OfferWeb();
        request.setProductId(productId);
        request.setGame("Elden Ring");
        request.setPlatform("PC");
        request.setRegion("BR");
        request.setVariant("Standard");
        request.setPrice(new BigDecimal("25.00"));
        request.setDeliveryMethod("AUTOMATIC");
        return request;
    }

    private static OfferActionRequest action(String reason) {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason(reason);
        request.setSource("integration-test");
        return request;
    }
}
