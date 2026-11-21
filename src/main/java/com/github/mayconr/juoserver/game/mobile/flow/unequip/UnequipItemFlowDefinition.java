package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.validation.ValidateUnequipTargetStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.apply.UnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.notification.NotifyUnequipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.unequip.validation.ValidateUnequipItemStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public final class UnequipItemFlowDefinition {
    private UnequipItemFlowDefinition() {
    }

    public static Flow<UnequipItemContext> build(GameInfra infra, WorldMobile mobiles, ItemModule items) {
        return FlowFactory.<UnequipItemContext>builder()
                .step(new ValidateUnequipItemStep())
                .step(new ValidateUnequipTargetStep())
                .step(new UnequipItemStep(infra, mobiles, items))
                .step(new NotifyUnequipItemStep(infra.eventBus()))
                .build();
    }

}
