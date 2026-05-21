package com.github.mayconr.juoserver.game.damage.flow.damage.calculation;

import com.github.mayconr.juoserver.game.damage.flow.damage.DamageContext;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CalculateTotalDamageStep extends AbstractFlowStep<DamageContext> {
    public CalculateTotalDamageStep() {
        super("calculate_total_damage");
    }

    @Override
    public StepResult execute(DamageContext context) {
        int total = 0;

        for (DamageComponent damage : context.getComponents()) {
            total += damage.damage();
        }

        context.setTotalDamage(total);
        return StepResult.success();
    }
}
