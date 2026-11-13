package com.github.mayconr.juoserver.game.item.flow.drop;

import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.DropTarget;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.ResolveDropTypeStep;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.ResolveGroundTargetStep;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.ResolveItemStep;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.ResolveItemTargetStep;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.ResolveMobileTargetStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemContainerStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemGroundStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.ResolveContainerDropPositionStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.StackItemStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.TransformToOrphanStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.RecordGroundDropStep;
import com.github.mayconr.juoserver.game.item.flow.drop.notification.AcceptDropStep;
import com.github.mayconr.juoserver.game.item.flow.drop.notification.RejectDropHook;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateDropTargetStep;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.item.flow.lift.validation.ItemRootResolver;
import com.github.mayconr.juoserver.game.item.hold.HeldItems;
import com.github.mayconr.juoserver.game.item.hold.RequireHeldItemStep;
import com.github.mayconr.juoserver.game.item.hold.RestoreHeldItem;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.flow.FlowHook;
import com.github.mayconr.juoserver.infrastructure.flow.HookPosition;
import com.github.mayconr.juoserver.infrastructure.flow.HookTarget;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;

public class DropItemFlowDefinition {
    private DropItemFlowDefinition() {
    }

    public static Flow<DropItemContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<DropItemContext>builder()
                .appendGroup("DropItemPhase", dropItemPhase(infra.storage(), modules.item().heldItems()))
                .appendGroup("ValidationPhase", validationPhase(infra.storage(), modules.item().heldItems()))
                .appendGroup("ItemPlacementPhase", itemPlacementPhase(infra.storage(), infra.eventBus(), modules.container()))
                .step(new RecordGroundDropStep(modules.item()))
                .step(new AcceptDropStep(modules.item().heldItems(), infra.eventBus()), DropItemContext::isDroppedItem)
                .hook(new FlowHook<>(
                        HookTarget.any(),
                        new RejectDropHook(modules.item().heldItems(),
                                new RestoreHeldItem(infra.storage(), modules.item()::recordGroundInteraction), infra.eventBus()),
                        HookPosition.AFTER_FAILURE
                ))
                .build();
    }

    private static FlowBuilder<DropItemContext> dropItemPhase(WorldStorage storage, HeldItems heldItems) {
        return FlowFactory.<DropItemContext>builder()
                .step(new RequireHeldItemStep<>(heldItems), DropItemContext::isDroppedItem)
                .step(new ResolveItemStep(storage), DropItemContext::isDroppedItem)
                .step(new ResolveDropTypeStep(), DropItemContext::isDroppedItem)
                .step(new ResolveGroundTargetStep(), ctx -> ctx.isDroppedItem(DropTarget.GROUND))
                .step(new ResolveMobileTargetStep(storage), ctx -> ctx.isDroppedItem(DropTarget.MOBILE))
                .step(new ResolveItemTargetStep(storage), ctx -> ctx.isDroppedItem(DropTarget.ITEM));
    }

    private static FlowBuilder<DropItemContext> validationPhase(WorldStorage storage, HeldItems heldItems) {
        return FlowFactory.<DropItemContext>builder()
                .step(new ValidateDropTargetStep(storage, heldItems))
                .step(new ValidateInteractionRangeStep(new ItemRootResolver(storage)));
    }

    private static FlowBuilder<DropItemContext> itemPlacementPhase(
            WorldStorage storage,
            EventBus eventBus,
            WorldContainer containers
    ) {
        return FlowFactory.<DropItemContext>builder()
                .step(new TransformToOrphanStep(storage))
                .step(new DropItemGroundStep(storage, eventBus), DropItemContext::isGroundDrop)
                .step(new ResolveContainerDropPositionStep(), DropItemContext::isContainerDrop)
                .step(new DropItemContainerStep(eventBus, containers), DropItemContext::isContainerDrop)
                .step(new StackItemStep(storage, eventBus), DropItemContext::isStack);

    }

}
