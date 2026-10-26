package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ApplyRegenStep extends AbstractFlowStep<RegenContext> {
    public ApplyRegenStep() {
        super("apply-regen");
    }

    @Override
    public StepResult execute(RegenContext context) {
        var mobile = context.getMobile();
        var attributes = mobile.runtimeAttributes();
        attributes.set(RegenContext.HP_ACCUMULATOR, context.getHitpoints().remainder());
        attributes.set(RegenContext.STAMINA_ACCUMULATOR, context.getStamina().remainder());
        attributes.set(RegenContext.MANA_ACCUMULATOR, context.getMana().remainder());
        if (context.getHitpoints().changed()) mobile.setHitpoints(context.getHitpoints().next());
        if (context.getStamina().changed()) mobile.setStamina(context.getStamina().next());
        if (context.getMana().changed()) mobile.setMana(context.getMana().next());
        return StepResult.success();
    }
}
