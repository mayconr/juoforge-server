package com.github.mayconr.juoserver.game.item.flow.lift;

import com.github.mayconr.juoserver.game.item.flow.lift.apply.HoldItemStep;
import com.github.mayconr.juoserver.game.item.flow.lift.apply.UnequipLiftedItemStep;
import com.github.mayconr.juoserver.game.item.flow.lift.load.ResolveLiftedItemStep;
import com.github.mayconr.juoserver.game.item.flow.lift.notification.RejectLiftHook;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRootResolver;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ValidateItemReachStep;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ValidateLiftableItemStep;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ValidateLifterStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.flow.FlowHook;
import com.github.mayconr.juoserver.infrastructure.flow.HookPosition;
import com.github.mayconr.juoserver.infrastructure.flow.HookTarget;

/** Validates a client lift request and puts the item on the player's cursor, or refuses it to the client. */
public final class LiftItemFlowDefinition {
    private LiftItemFlowDefinition() {
    }

    public static Flow<LiftItemContext> build(GameModules modules, GameInfra infra) {
        var heldItems = modules.item().heldItems();
        var storage = infra.storage();
        return FlowFactory.<LiftItemContext>builder()
                .step(new ValidateLifterStep(heldItems))
                .step(new ResolveLiftedItemStep(storage))
                .step(new ValidateLiftableItemStep())
                .step(new ValidateItemReachStep(new ItemRootResolver(storage)))
                .step(new HoldItemStep(heldItems, storage))
                .step(new UnequipLiftedItemStep(modules.mobile()), context -> context.getItem().isEquipped())
                .hook(new FlowHook<>(
                        HookTarget.any(),
                        new RejectLiftHook(heldItems, infra.eventBus()),
                        HookPosition.AFTER_FAILURE
                ))
                .build();
    }
}
