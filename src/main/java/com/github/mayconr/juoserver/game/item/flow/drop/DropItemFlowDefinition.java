package com.github.mayconr.juoserver.game.item.flow.drop;

import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.DropTarget;
import com.github.mayconr.juoserver.game.item.flow.drop.dropitem.*;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemContainerStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.DropItemGroundStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.StackItemStep;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.TransformToOrphanStep;
import com.github.mayconr.juoserver.game.item.flow.drop.validation.ValidateInteractionRangeStep;
import com.github.mayconr.juoserver.game.messaging.MessageModule;
import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameModules;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class DropItemFlowDefinition {
    private DropItemFlowDefinition() {
    }

    public static Flow<DropItemContext> build(GameModules modules, GameInfra infra) {
        return FlowFactory.<DropItemContext>builder()
                .appendGroup("DropItemPhase", dropItemPhase(infra.storage()))
                .appendGroup("ValidationPhase", validationPhase(modules.mobile(), modules.message()))
                .appendGroup("ItemPlacementPhase", itemPlacementPhase(infra.storage(), infra.eventBus()))
                .build();
    }

    private static FlowBuilder<DropItemContext> dropItemPhase(RealmStorage storage) {
        return FlowFactory.<DropItemContext>builder()
                .step(new ResolveItemStep(storage), DropItemContext::isDroppedItem)
                .step(new ResolveDropTypeStep(), DropItemContext::isDroppedItem)
                .step(new ResolveGroundTargetStep(), ctx-> ctx.isDroppedItem(DropTarget.GROUND))
                .step(new ResolveMobileTargetStep(storage), ctx-> ctx.isDroppedItem(DropTarget.MOBILE))
                .step(new ResolveItemTargetStep(storage), ctx-> ctx.isDroppedItem(DropTarget.ITEM));
    }

    private static FlowBuilder<DropItemContext> validationPhase(MobileModule mobileModule, MessageModule messageModule) {
        return FlowFactory.<DropItemContext>builder()
                .step(new ValidateInteractionRangeStep(mobileModule, messageModule));
    }

    private static FlowBuilder<DropItemContext> itemPlacementPhase(RealmStorage storage, EventBus eventBus) {
        return FlowFactory.<DropItemContext>builder()
                .step(new TransformToOrphanStep(storage))
                .step(new DropItemGroundStep(storage, eventBus), DropItemContext::isGroundDrop)
                .step(new DropItemContainerStep(eventBus), DropItemContext::isContainerDrop)
                .step(new StackItemStep(storage, eventBus), DropItemContext::isStack);

    }

}
