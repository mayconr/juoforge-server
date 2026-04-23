package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class IdentifyLocationTypeStep extends AbstractFlowStep<ItemCreationContext> {
    public IdentifyLocationTypeStep() {
        super("identify_location_type");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        switch (context.getTarget()) {
            case EquipItemTarget target -> context.setItemLocation(new EquippedLocation(target.mobile().getSerialId()));
            case GroundItemTarget target -> context.setItemLocation(new GroundLocation());
            case ContainerItemTarget target -> context.setItemLocation(new ContainerLocation(target.container().getSerialId()));
            case OrphanItemTarget target -> context.setItemLocation(new OrphanLocation());
        }
        return StepResult.success();
    }
}
