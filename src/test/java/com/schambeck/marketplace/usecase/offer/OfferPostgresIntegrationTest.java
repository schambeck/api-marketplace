package com.schambeck.marketplace.usecase.offer;

import com.schambeck.marketplace.exception.OfferValidationException;
import com.schambeck.marketplace.usecase.gamecontext.GameContext;
import com.schambeck.marketplace.usecase.gamecontext.GameContextPublicationValidator;
import com.schambeck.marketplace.usecase.gamecontext.GameContextService;
import com.schambeck.marketplace.usecase.product.Product;
import com.schambeck.marketplace.usecase.product.ProductRepository;
import com.schambeck.marketplace.usecase.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
@Import({OfferService.class, GameContextService.class, GameContextPublicationValidator.class})
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
    private com.schambeck.marketplace.usecase.gamecontext.GameContextRepository contextRepository;

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
    void appliesMigrationPublishesOfferAndBlocksItAfterAvailabilityChange() {
        Product product = productRepository.save(Product.builder()
                .name("Game coins")
                .description("Game coins")
                .price(new BigDecimal("10.00"))
                .quantity(new BigDecimal("10.00"))
                .user(userRepository.findByIdOrThrow(1))
                .category("Digital")
                .inventoryStatus("INSTOCK")
                .rating("5")
                .image("coins.jpg")
                .objectId("coins")
                .version(0)
                .build());
        GameContext context = contextRepository.save(GameContext.builder()
                .game("Elden Ring")
                .platform("PC")
                .region("BR")
                .status(com.schambeck.marketplace.usecase.gamecontext.GameContextStatus.ACTIVE)
                .version(0)
                .build());

        OfferWeb request = new OfferWeb();
        request.setProductId(product.getId());
        request.setGameContextId(context.getId());
        request.setPrice(new BigDecimal("25.00"));
        request.setDeliveryMethod("AUTOMATIC");
        request.setAvailabilitySource("SUPPLY");
        request.setAvailabilityStatus(OfferAvailabilityStatus.AVAILABLE);

        Offer offer = service.create(request);
        OfferActionRequest publish = action("initial publication", "integration-test");
        service.publish(offer.getId(), publish, "admin");

        assertThat(service.findPublishedById(offer.getId()).getStatus()).isEqualTo(OfferStatus.PUBLISHED);
        assertThat(historyRepository.findAllByOfferIdOrderByIdAsc(offer.getId()))
                .extracting(OfferStatusHistory::getToStatus)
                .containsExactly(OfferStatus.DRAFT, OfferStatus.PUBLISHED);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '12'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns where table_name = 'cart_item' and column_name = 'offer_id'",
                Integer.class)).isEqualTo(1);

        OfferAvailabilityRequest unavailable = new OfferAvailabilityRequest();
        unavailable.setAvailabilityStatus(OfferAvailabilityStatus.UNAVAILABLE);
        unavailable.setReason("supply exhausted");
        unavailable.setSource("integration-test");
        service.changeAvailability(offer.getId(), unavailable, "operator");

        assertThatThrownBy(() -> service.validateForCheckout(offer.getId()))
                .isInstanceOf(OfferValidationException.class)
                .hasMessageContaining("unavailable");
    }

    private static OfferActionRequest action(String reason, String source) {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason(reason);
        request.setSource(source);
        return request;
    }
}
