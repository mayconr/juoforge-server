package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class CreateMobileStep extends AbstractFlowStep<PlayerCreationContext> {

    private final RealmStorage storage;

    public CreateMobileStep(RealmStorage storage) {
        super("CreateMobileStep");
        this.storage = storage;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        final var data = context.getMobileData();

        if (data == null) {
            return StepResult.failure("Mobile data is null");
        }

        if (storage.createMobile(data) instanceof UOPlayer player) {
            context.setPlayer(player);
            return StepResult.success();
        }
        return StepResult.failure("Unable to create mobile");
    }
}
