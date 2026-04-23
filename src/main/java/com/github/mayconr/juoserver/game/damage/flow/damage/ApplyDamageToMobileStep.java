package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ApplyDamageToMobileStep extends AbstractFlowStep<DamageContext> {
    public ApplyDamageToMobileStep() {
        super("apply_damage_to_mobile", 200, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(DamageContext context) {
        var target = context.getTarget();

        int oldHp = target.getHitpoints();

        int newHp = Math.max(0, oldHp - context.getTotalDamage());

        target.setHitpoints(newHp);

        context.setOldHp(oldHp);
        context.setNewHp(newHp);
        return StepResult.success();
    }
}
