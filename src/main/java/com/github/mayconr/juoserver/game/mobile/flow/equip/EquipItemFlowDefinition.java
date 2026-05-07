package com.github.mayconr.juoserver.game.mobile.flow.equip;

import com.github.mayconr.juoserver.game.mobile.flow.equip.apply.EquipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.equip.load.LoadEquipItemStep;
import com.github.mayconr.juoserver.game.mobile.flow.equip.notification.NotifyItemEquipped;
import com.github.mayconr.juoserver.game.mobile.flow.equip.state.CleanupItemStateStep;
import com.github.mayconr.juoserver.game.mobile.flow.equip.validation.ValidateItemEquipStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class EquipItemFlowDefinition {
    private EquipItemFlowDefinition() {
    }

    public static Flow<EquipItemContext> build(GameInfra infra) {
        return FlowFactory.<EquipItemContext>builder()
                .step(new LoadEquipItemStep(infra.storage()), context->context.getItem() == null)
                .step(new ValidateItemEquipStep())
                .step(new CleanupItemStateStep(infra.storage()))
                .step(new EquipItemStep())
                .step(new NotifyItemEquipped(infra.eventBus()))
                .build();
    }

}
