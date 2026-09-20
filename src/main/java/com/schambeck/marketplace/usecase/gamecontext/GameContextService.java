package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.GameContextValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameContextService {
    private final GameContextRepository repository;
    private final GameContextHistoryRepository historyRepository;
    private final GameContextPublicationValidator publicationValidator;

    @Transactional
    public GameContext create(GameContext entity, String changedBy) {
        normalizeAndValidate(entity);
        if (entity.getStatus() == null) {
            entity.setStatus(GameContextStatus.ACTIVE);
        }
        if (entity.getStatus() == GameContextStatus.DISCONTINUED) {
            throw new GameContextValidationException("A new game context cannot start as DISCONTINUED.");
        }
        GameContext saved = repository.save(entity);
        recordHistory(saved, "CREATED", changedBy);
        return saved;
    }

    @Transactional
    public GameContext update(int id, GameContext entity, String changedBy) {
        GameContext current = findById(id);
        if (current.getStatus() == GameContextStatus.DISCONTINUED) {
            throw new GameContextValidationException("A DISCONTINUED game context cannot be changed.");
        }

        normalizeAndValidate(entity);
        if (entity.getStatus() == null) {
            entity.setStatus(current.getStatus());
        }
        validateStatusTransition(current.getStatus(), entity.getStatus());

        GameContextMapper.INSTANCE.copy(entity, current);
        GameContext saved = repository.save(current);
        recordHistory(saved, "UPDATED", changedBy);
        return saved;
    }

    public GameContext findById(int id) {
        return repository.findByIdOrThrow(id);
    }

    /**
     * Validates and returns the immutable data needed by an Offer to preserve its context reference.
     */
    public GameContextReference referenceForOfferPublication(int id) {
        GameContext context = publicationValidator.validate(findById(id));
        return new GameContextReference(
                context.getId(),
                context.getVersion(),
                context.getGame(),
                context.getPublisher(),
                context.getPlatform(),
                context.getRegion(),
                context.getServer(),
                context.getVariant());
    }

    public List<GameContextHistory> history(int id) {
        findById(id);
        return historyRepository.findAllByGameContextIdOrderByIdAsc(id);
    }

    public Page<GameContext> searchActive(Pageable pageable, String game, String platform, String region) {
        return repository.searchByStatusAndAttributes(
                GameContextStatus.ACTIVE,
                normalizedFilter(game),
                normalizedFilter(platform),
                normalizedFilter(region),
                pageable);
    }

    private void normalizeAndValidate(GameContext entity) {
        entity.setGame(trim(entity.getGame()));
        entity.setPublisher(trim(entity.getPublisher()));
        entity.setPlatform(trim(entity.getPlatform()));
        entity.setRegion(trim(entity.getRegion()));
        entity.setServer(trim(entity.getServer()));
        entity.setVariant(trim(entity.getVariant()));

        if (!notBlank(entity.getGame())) {
            throw new GameContextValidationException("game is required.");
        }
        if (!notBlank(entity.getPlatform())) {
            throw new GameContextValidationException("platform is required.");
        }
        if (notBlank(entity.getServer()) && !notBlank(entity.getRegion())) {
            throw new GameContextValidationException("region is required when server is provided.");
        }
    }

    private void validateStatusTransition(GameContextStatus current, GameContextStatus requested) {
        if (requested == GameContextStatus.DISCONTINUED && current == GameContextStatus.DISCONTINUED) {
            throw new GameContextValidationException("A DISCONTINUED game context cannot be changed.");
        }
    }

    private void recordHistory(GameContext entity, String action, String changedBy) {
        historyRepository.save(GameContextHistory.builder()
                .gameContextId(entity.getId())
                .action(action)
                .game(entity.getGame())
                .publisher(entity.getPublisher())
                .platform(entity.getPlatform())
                .region(entity.getRegion())
                .server(entity.getServer())
                .variant(entity.getVariant())
                .status(entity.getStatus())
                .changedBy(changedBy)
                .recordedAt(LocalDateTime.now())
                .build());
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalizedFilter(String value) {
        return notBlank(value) ? value.trim() : null;
    }
}
