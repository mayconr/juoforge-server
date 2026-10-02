package com.github.mayconr.shard.spells.heal.notification;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.shard.spells.heal.HealContext;

public class SendHealMessageStep extends AbstractFlowStep<HealContext> {
    private final WorldMessage messages;

    public SendHealMessageStep(WorldMessage messages) {
        super("SendHealMessage");
        this.messages = messages;
    }

    @Override
    public StepResult execute(HealContext context) {
        if (context.getCaster() instanceof UOPlayer player) {
            messages.send(player, "Foi heal");
        }
        return StepResult.success();
    }
}
