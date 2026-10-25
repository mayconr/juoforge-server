package com.github.mayconr.juoserver.game.player.flow.deletion;

import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public final class DeletePlayerFlowDefinition {
    private DeletePlayerFlowDefinition() {}

    public static Flow<DeletePlayerContext> build(RealmStorage storage) {
        return FlowFactory.<DeletePlayerContext>builder()
                .step(new ValidatePlayerDeletionStep(storage))
                .step(new DeletePlayerStep(storage))
                .build();
    }
}

