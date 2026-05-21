package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.mobile.flow.unequip.apply.UnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.load.LoadUnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.notification.NotifyUnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.validation.ValidateUnequipItemStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class UnequipItemFlowDefinition {
    private UnequipItemFlowDefinition() {
    }

    public static Flow<UnequipItemContext> build(GameInfra infra) {
        return FlowFactory.<UnequipItemContext>builder()
                .step(new LoadUnequipItemStep(infra.storage()), context->context.getItem() == null)
                .step(new ValidateUnequipItemStep())
                .step(new UnequipItemStep())
                .step(new NotifyUnequipItemStep(infra.eventBus()))
                .build();
    }

}
