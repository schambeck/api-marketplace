package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.GameContextValidationException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({GameContextService.class, GameContextPublicationValidator.class})
@Testcontainers
class GameContextPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.3-alpine")
            .withDatabaseName("marketplace")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private GameContextService service;

    @Autowired
    private GameContextRepository repository;

    @Autowired
    private GameContextHistoryRepository historyRepository;

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
    void flywayCreatesContextTablesAndHistoryRetainsDiscontinuedContext() {
        GameContext created = service.create(GameContext.builder()
                .game("Elden Ring")
                .publisher("FromSoftware")
                .platform("PC")
                .region("BR")
                .server("South America")
                .variant("Standard")
                .build(), "admin@example.com");

        GameContextReference reference = service.referenceForOfferPublication(created.getId());
        assertThat(reference.version()).isEqualTo(created.getVersion());

        service.update(created.getId(), GameContext.builder()
                .game(created.getGame())
                .publisher(created.getPublisher())
                .platform(created.getPlatform())
                .region(created.getRegion())
                .server(created.getServer())
                .variant(created.getVariant())
                .status(GameContextStatus.DISCONTINUED)
                .version(created.getVersion())
                .build(), "admin@example.com");

        GameContext retained = repository.findByIdOrThrow(created.getId());

        assertThat(retained.getStatus()).isEqualTo(GameContextStatus.DISCONTINUED);
        assertThat(historyRepository.findAllByGameContextIdOrderByIdAsc(created.getId()))
                .extracting(GameContextHistory::getAction)
                .containsExactly("CREATED", "UPDATED");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where version = '11'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from game_context_history where game_context_id = ?",
                Integer.class, created.getId()))
                .isEqualTo(2);
        assertThatThrownBy(() -> service.referenceForOfferPublication(created.getId()))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessageContaining("is not active");
    }
}
