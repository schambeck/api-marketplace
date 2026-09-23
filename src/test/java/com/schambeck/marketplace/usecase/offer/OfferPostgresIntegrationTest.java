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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Test
    void searchesAdminOffersAcrossStatusesWithFilterAndPagination() {
        Offer draft = createOffer("Admin State Draft", "Admin state", "Admin state category", "Admin State Game",
                "Admin State Publisher", "Admin State Platform", "Admin State Region", "Admin State Server",
                "Admin State Variant", false);
        Offer published = createPublishedOffer("Admin State Published", "Admin state", "Admin state category", "Admin State Game",
                "Admin State Publisher", "Admin State Platform", "Admin State Region", "Admin State Server",
                "Admin State Variant");
        Offer paused = createPublishedOffer("Admin State Paused", "Admin state", "Admin state category", "Admin State Game",
                "Admin State Publisher", "Admin State Platform", "Admin State Region", "Admin State Server",
                "Admin State Variant");
        service.pause(paused.getId(), action("pause admin state offer", "integration-test"), "admin");
        Offer retired = createOffer("Admin State Retired", "Admin state", "Admin state category", "Admin State Game",
                "Admin State Publisher", "Admin State Platform", "Admin State Region", "Admin State Server",
                "Admin State Variant", false);
        service.retire(retired.getId(), action("retire admin state offer", "integration-test"), "admin");

        Page<Offer> allStates = service.searchAdmin(PageRequest.of(0, 10), null, "admin state");
        assertThat(allStates.getTotalElements()).isEqualTo(4);
        assertThat(allStates.getContent()).extracting(Offer::getId)
                .containsExactly(draft.getId(), published.getId(), paused.getId(), retired.getId());
        assertThat(allStates.getContent()).extracting(Offer::getStatus)
                .containsExactly(OfferStatus.DRAFT, OfferStatus.PUBLISHED, OfferStatus.PAUSED, OfferStatus.RETIRED);

        Page<Offer> pausedOnly = service.searchAdmin(PageRequest.of(0, 10), OfferStatus.PAUSED, "ADMIN STATE");
        assertThat(pausedOnly.getTotalElements()).isEqualTo(1);
        assertThat(pausedOnly.getContent()).extracting(Offer::getId).containsExactly(paused.getId());

        Page<Offer> secondPage = service.searchAdmin(PageRequest.of(1, 2), null, "admin state");
        assertThat(secondPage.getTotalElements()).isEqualTo(4);
        assertThat(secondPage.getContent()).extracting(Offer::getId)
                .containsExactly(paused.getId(), retired.getId());
    }

    @Test
    void searchesAndLoadsOnlyOffersOwnedByAuthenticatedSeller() {
        Offer sellerOneOffer = createOfferForUser(1, "Seller One Offer", "Seller One Game", false);
        Offer sellerTwoOffer = createOfferForUser(2, "Seller Two Offer", "Seller Two Game", false);

        Page<Offer> sellerOneOffers = service.searchOwned(
                "mschambeck@gmail.com", PageRequest.of(0, 10), null, "seller");
        assertThat(sellerOneOffers.getContent()).extracting(Offer::getId).containsExactly(sellerOneOffer.getId());

        Page<Offer> sellerTwoOffers = service.searchOwned(
                "lupin@gmail.com", PageRequest.of(0, 10), OfferStatus.DRAFT, null);
        assertThat(sellerTwoOffers.getContent()).extracting(Offer::getId).containsExactly(sellerTwoOffer.getId());

        assertThatThrownBy(() -> service.findOwnedById("mschambeck@gmail.com", sellerTwoOffer.getId()))
                .isInstanceOf(com.schambeck.marketplace.exception.EntityNotFoundException.class);
        assertThat(service.findOwnedById("lupin@gmail.com", sellerTwoOffer.getId()).getId())
                .isEqualTo(sellerTwoOffer.getId());
    }

    @Test
    void searchesPublishedOffersAcrossProductAndContextFields() {
        Offer searchable = createPublishedOffer(
                "Search Product Alpha",
                "Search description unique",
                "Search category unique",
                "Search Game Unique",
                "Search Publisher Unique",
                "Search Platform Unique",
                "Search Region Unique",
                "Search Server Unique",
                "Search Variant Unique");
        Offer draft = createOffer(
                "Draft-only search product", "Draft description", "Draft category", "Draft Game",
                "Draft Publisher", "Draft Platform", "Draft Region", "Draft Server", "Draft Variant", false);

        PageRequest pageable = PageRequest.of(0, 1);
        Page<Offer> allPublished = service.searchPublished(PageRequest.of(0, 100));
        assertThat(service.searchPublished(PageRequest.of(0, 100), "").getTotalElements())
                .isEqualTo(allPublished.getTotalElements());
        assertThat(service.searchPublished(PageRequest.of(0, 100), "   ").getTotalElements())
                .isEqualTo(allPublished.getTotalElements());

        assertSearchFindsOnly(searchable.getId(), "product alpha");
        assertSearchFindsOnly(searchable.getId(), "GAME UNIQUE");
        assertSearchFindsOnly(searchable.getId(), "platform unique");
        assertSearchFindsOnly(searchable.getId(), "region unique");
        assertSearchFindsOnly(searchable.getId(), "server unique");
        assertSearchFindsOnly(searchable.getId(), "variant unique");
        assertSearchFindsOnly(searchable.getId(), "publisher unique");
        assertSearchFindsOnly(searchable.getId(), "description unique");
        assertSearchFindsOnly(searchable.getId(), "category unique");

        Page<Offer> paged = service.searchPublished(pageable, "search product alpha");
        assertThat(paged.getTotalElements()).isEqualTo(1);
        assertThat(paged.getNumber()).isZero();
        assertThat(paged.getSize()).isEqualTo(1);
        assertThat(paged.getContent()).extracting(Offer::getId).containsExactly(searchable.getId());

        assertThat(service.searchPublished(PageRequest.of(0, 10), "does-not-exist")).isEmpty();
        assertThat(service.searchPublished(PageRequest.of(0, 10), "draft-only search product")).isEmpty();
        assertThat(draft.getStatus()).isEqualTo(OfferStatus.DRAFT);
    }

    private void assertSearchFindsOnly(Integer offerId, String search) {
        Page<Offer> result = service.searchPublished(PageRequest.of(0, 10), search);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Offer::getId).containsExactly(offerId);
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
        return createPublishedOffer("Concurrent coins", "Concurrent coins", "Digital", "Elden Ring", null,
                "PC", "BR", null, null);
    }

    private Offer createPublishedOffer(String productName, String description, String category, String game,
                                       String publisher, String platform, String region, String server, String variant) {
        return createOffer(productName, description, category, game, publisher, platform, region, server, variant, true);
    }

    private Offer createOffer(String productName, String description, String category, String game,
                              String publisher, String platform, String region, String server, String variant,
                              boolean publish) {
        return createOfferForUser(1, productName, description, category, game, publisher, platform, region, server,
                variant, publish);
    }

    private Offer createOfferForUser(int userId, String productName, String game, boolean publish) {
        return createOfferForUser(userId, productName, productName, "Digital", game, null, "PC", "BR", null, null,
                publish);
    }

    private Offer createOfferForUser(int userId, String productName, String description, String category, String game,
                                     String publisher, String platform, String region, String server, String variant,
                                     boolean publish) {
        Product product = productRepository.save(Product.builder()
                .name(productName)
                .description(description)
                .price(new BigDecimal("15.00"))
                .quantity(new BigDecimal("10.00"))
                .user(userRepository.findByIdOrThrow(userId))
                .category(category)
                .inventoryStatus("INSTOCK")
                .rating("5")
                .image(productName + ".jpg")
                .objectId(productName)
                .version(0)
                .build());
        GameContext context = contextRepository.save(GameContext.builder()
                .game(game)
                .publisher(publisher)
                .platform(platform)
                .region(region)
                .server(server)
                .variant(variant)
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
        return publish ? service.publish(offer.getId(), action("concurrent test publication", "integration-test"), "admin") : offer;
    }

    private static OfferActionRequest action(String reason, String source) {
        OfferActionRequest request = new OfferActionRequest();
        request.setReason(reason);
        request.setSource(source);
        return request;
    }
}
