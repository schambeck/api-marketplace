package com.schambeck.marketplace.usecase.gamecontext;

import com.schambeck.marketplace.exception.GameContextValidationException;
import org.springframework.stereotype.Component;

@Component
public class GameContextPublicationValidator {
    public GameContext validate(GameContext context) {
        if (context.getStatus() != GameContextStatus.ACTIVE) {
            throw new GameContextValidationException(
                    "Game context id " + context.getId() + " is not active and cannot be used for a new offer.");
        }
        if (isBlank(context.getGame()) || isBlank(context.getPlatform())) {
            throw new GameContextValidationException("An offer requires a game context with game and platform.");
        }
        if (!isBlank(context.getServer()) && isBlank(context.getRegion())) {
            throw new GameContextValidationException("region is required when server is provided.");
        }
        return context;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
