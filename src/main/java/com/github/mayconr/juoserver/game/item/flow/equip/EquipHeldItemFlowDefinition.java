package com.github.mayconr.juoserver.game.item.flow.equip;

import com.github.mayconr.juoserver.game.item.flow.equip.apply.EquipHeldItemStep;
import com.github.mayconr.juoserver.game.item.flow.equip.load.ResolveHeldItemStep;
import com.github.mayconr.juoserver.game.item.hold.RequireHeldItemStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

/** Equips the item the player is holding, refusing items that are not on the player's cursor. */
public final class EquipHeldItemFlowDefinition {
    private EquipHeldItemFlowDefinition() {
    }

    public static Flow<EquipHeldItemContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<EquipHeldItemContext>builder()
                .step(new RequireHeldItemStep<>(modules.item().heldItems()))
                .step(new ResolveHeldItemStep(infra.storage()))
                .step(new EquipHeldItemStep(modules.mobile()))
                .build();
    }
}
