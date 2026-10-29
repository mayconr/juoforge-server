package com.github.mayconr.juoserver.game.mobile.flow.setattribute;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ApplyAttributeStep extends AbstractFlowStep<SetAttributeContext> {
    public ApplyAttributeStep() {
        super("apply-attribute");
    }

    @Override
    public StepResult execute(SetAttributeContext context) {
        var mobile = context.getMobile();
        switch (context.getAttribute()) {
            case STR -> mobile.setStrength(context.getValue());
            case DEX -> mobile.setDexterity(context.getValue());
            case INT -> mobile.setIntelligence(context.getValue());
        }
        return StepResult.success();
    }
}
