package com.github.mayconr.shard.spells.heal.notification;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.shard.spells.heal.HealContext;

public class SendHealMessageStep extends AbstractFlowStep<HealContext> {
    private final WorldActions world;

    public SendHealMessageStep(WorldActions world) {
        super("SendHealMessage");
        this.world = world;
    }

    @Override
    public StepResult execute(HealContext context) {
        if (context.getCaster() instanceof UOPlayer player) {
            world.sendMessage(player, "Foi heal");
        }
        return StepResult.success();
    }
}
