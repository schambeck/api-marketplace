package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.GameContextValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameContextServiceTest {
    @Mock
    private GameContextRepository repository;

    @Mock
    private GameContextHistoryRepository historyRepository;

    @Mock
    private GameContextPublicationValidator publicationValidator;

    @InjectMocks
    private GameContextService service;

    @Test
    void createsActiveContextAndRecordsAuditSnapshot() {
        GameContext context = GameContext.builder()
                .game("  Elden Ring ")
                .platform(" PC ")
                .region("BR")
                .server("South America")
                .build();
        when(repository.save(context)).thenAnswer(invocation -> {
            context.setId(10);
            return context;
        });

        GameContext created = service.create(context, "seller@example.com");

        assertThat(created.getStatus()).isEqualTo(GameContextStatus.ACTIVE);
        assertThat(created.getGame()).isEqualTo("Elden Ring");
        assertThat(created.getPlatform()).isEqualTo("PC");
        ArgumentCaptor<GameContextHistory> audit = ArgumentCaptor.forClass(GameContextHistory.class);
        verify(historyRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo("CREATED");
        assertThat(audit.getValue().getGameContextId()).isEqualTo(10);
        assertThat(audit.getValue().getChangedBy()).isEqualTo("seller@example.com");
    }

    @Test
    void rejectsServerWithoutRegionWithActionableMessage() {
        GameContext context = GameContext.builder()
                .game("Elden Ring")
                .platform("PC")
                .server("South America")
                .build();

        assertThatThrownBy(() -> service.create(context, "seller@example.com"))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessage("region is required when server is provided.");
        verifyNoInteractions(repository, historyRepository);
    }

    @Test
    void rejectsChangesToDiscontinuedContext() {
        GameContext current = GameContext.builder()
                .id(10)
                .game("Elden Ring")
                .platform("PC")
                .status(GameContextStatus.DISCONTINUED)
                .build();
        when(repository.findByIdOrThrow(10)).thenReturn(current);

        assertThatThrownBy(() -> service.update(10, GameContext.builder()
                        .game("Elden Ring")
                        .platform("PC")
                        .status(GameContextStatus.ACTIVE)
                        .build(), "admin@example.com"))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessage("A DISCONTINUED game context cannot be changed.");
        verify(repository, never()).save(any());
        verifyNoInteractions(historyRepository);
    }

    @Test
    void returnsVersionedReferenceForActiveOfferPublication() {
        GameContext context = GameContext.builder()
                .id(10)
                .version(4)
                .game("Elden Ring")
                .platform("PC")
                .region("BR")
                .variant("Standard")
                .status(GameContextStatus.ACTIVE)
                .build();
        when(repository.findByIdOrThrow(10)).thenReturn(context);
        when(publicationValidator.validate(context)).thenReturn(context);

        GameContextReference reference = service.referenceForOfferPublication(10);

        assertThat(reference.id()).isEqualTo(10);
        assertThat(reference.version()).isEqualTo(4);
        assertThat(reference.game()).isEqualTo("Elden Ring");
        assertThat(reference.variant()).isEqualTo("Standard");
        verify(publicationValidator).validate(context);
    }

    @Test
    void transitionsActiveContextToInactiveAndAuditsTheChange() {
        GameContext current = GameContext.builder()
                .id(10)
                .version(1)
                .game("Elden Ring")
                .platform("PC")
                .status(GameContextStatus.ACTIVE)
                .build();
        GameContext update = GameContext.builder()
                .game("Elden Ring")
                .platform("PC")
                .status(GameContextStatus.INACTIVE)
                .build();
        when(repository.findByIdOrThrow(10)).thenReturn(current);
        when(repository.save(current)).thenReturn(current);

        GameContext updated = service.update(10, update, "admin@example.com");

        assertThat(updated.getStatus()).isEqualTo(GameContextStatus.INACTIVE);
        ArgumentCaptor<GameContextHistory> audit = ArgumentCaptor.forClass(GameContextHistory.class);
        verify(historyRepository).save(audit.capture());
        assertThat(audit.getValue().getAction()).isEqualTo("UPDATED");
        assertThat(audit.getValue().getStatus()).isEqualTo(GameContextStatus.INACTIVE);
    }
}
