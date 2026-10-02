package com.github.mayconr.shard.spells.heal;

import com.github.mayconr.juoserver.game.messaging.WorldMessage;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.shard.spells.heal.notification.SendHealMessageStep;
import com.github.mayconr.shard.spells.heal.validation.ValidateHealCasterStep;

public final class HealFlowDefinition {
    private HealFlowDefinition() {}

    public static Flow<HealContext> build(WorldMessage messages) {
        return FlowFactory.<HealContext>builder()
                .step(new ValidateHealCasterStep())
                .step(new SendHealMessageStep(messages))
                .build();
    }
}
