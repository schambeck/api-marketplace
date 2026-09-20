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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        int legacyProductCount = jdbcTemplate.queryForObject("select count(*) from product where price > 0", Integer.class);
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
                "select count(*) from flyway_schema_history where version = '13'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from offer where availability_source = 'LEGACY_PRODUCT_MIGRATION'", Integer.class))
                .isEqualTo(legacyProductCount);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from offer_status_history where source = 'LEGACY_PRODUCT_MIGRATION'", Integer.class))
                .isEqualTo(legacyProductCount);
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

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void serializesConcurrentAvailabilityChangesWithPessimisticLock() throws Exception {
        Offer offer = createPublishedOffer();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> first = executor.submit(() -> changeAvailabilityAfter(start, offer.getId(), OfferAvailabilityStatus.UNAVAILABLE, "operator-a"));
        Future<?> second = executor.submit(() -> changeAvailabilityAfter(start, offer.getId(), OfferAvailabilityStatus.AVAILABLE, "operator-b"));
        start.countDown();

        first.get();
        second.get();
        executor.shutdownNow();

        List<OfferStatusHistory> history = historyRepository.findAllByOfferIdOrderByIdAsc(offer.getId());
        assertThat(history).hasSize(4);
        assertThat(history).extracting(OfferStatusHistory::getChangedBy)
                .contains("operator-a", "operator-b");
        assertThat(service.findById(offer.getId()).getAvailabilityStatus())
                .isIn(OfferAvailabilityStatus.AVAILABLE, OfferAvailabilityStatus.UNAVAILABLE);
    }

    private void changeAvailabilityAfter(CountDownLatch start, Integer offerId,
                                         OfferAvailabilityStatus status, String actor) {
        try {
            start.await();
            OfferAvailabilityRequest request = new OfferAvailabilityRequest();
            request.setAvailabilityStatus(status);
            request.setReason("concurrent inventory update");
            request.setSource("integration-test");
            service.changeAvailability(offerId, request, actor);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private Offer createPublishedOffer() {
        Product product = productRepository.save(Product.builder()
                .name("Concurrent coins")
                .description("Concurrent coins")
                .price(new BigDecimal("15.00"))
                .quantity(new BigDecimal("10.00"))
                .user(userRepository.findByIdOrThrow(1))
                .category("Digital")
                .inventoryStatus("INSTOCK")
                .rating("5")
                .image("concurrent-coins.jpg")
                .objectId("concurrent-coins")
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
        request.setPrice(new BigDecimal("15.00"));
        request.setDeliveryMethod("AUTOMATIC");
        request.setAvailabilitySource("SUPPLY");
        request.setAvailabilityStatus(OfferAvailabilityStatus.AVAILABLE);
        Offer offer = service.create(request);
        return service.publish(offer.getId(), action("concurrent test publication", "integration-test"), "admin");
    }

    private static OfferActionRequest action(String reason, String source) {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason(reason);
        request.setSource(source);
        return request;
    }
}
