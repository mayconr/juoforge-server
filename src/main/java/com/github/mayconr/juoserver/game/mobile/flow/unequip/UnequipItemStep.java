package com.github.mayconr.juoserver.game.mobile.flow.unequip;

import com.github.mayconr.juoserver.game.flow.UnequipItemFlowDefinition.UnequipItemContext;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class UnequipItemStep extends AbstractFlowStep<UnequipItemContext> {
    public UnequipItemStep(int order) {
        super("unequip_item", order, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(UnequipItemContext context) {
        final var player = context.getMobile();
        final var item = context.getItem();

        player.unequipItem(item);
        item.setCurrentLocation(new OrphanLocation());
        context.setUnequipped(true);

        return StepResult.success();
    }
}
