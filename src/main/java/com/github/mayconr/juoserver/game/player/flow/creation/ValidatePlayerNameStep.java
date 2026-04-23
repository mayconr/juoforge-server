package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.player.exception.PlayerNameAlreadyExistsException;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;


public class ValidatePlayerNameStep extends AbstractFlowStep<PlayerCreationContext> {
    private final RealmStorage storage;

    public ValidatePlayerNameStep(RealmStorage storage) {
        super("ValidatePlayerNameStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        final var name = context.getCharacter().getCharacterName();
        var future = storage.mobileExists(name)
                .thenApply(exists -> {
                    if (exists) {
                        return context.fail(new PlayerNameAlreadyExistsException());
                    }
                    return StepResult.success();
                });
        return StepResult.async(future);
    }
}
