package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.GameContextValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameContextPublicationValidatorTest {
    private final GameContextPublicationValidator validator = new GameContextPublicationValidator();

    @Test
    void rejectsInactiveContext() {
        GameContext context = GameContext.builder()
                .id(3)
                .game("Elden Ring")
                .platform("PC")
                .status(GameContextStatus.INACTIVE)
                .build();

        assertThatThrownBy(() -> validator.validate(context))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessageContaining("is not active");
    }

    @Test
    void rejectsDiscontinuedContext() {
        GameContext context = GameContext.builder()
                .id(3)
                .game("Elden Ring")
                .platform("PC")
                .status(GameContextStatus.DISCONTINUED)
                .build();

        assertThatThrownBy(() -> validator.validate(context))
                .isInstanceOf(GameContextValidationException.class)
                .hasMessageContaining("is not active");
    }
}
