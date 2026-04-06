package com.github.mayconr.juoserver.game.damage.flow.damage;

import com.github.mayconr.juoserver.game.flow.DamageFlowDefinition.DamageContext;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class CalculateTotalDamageStep extends AbstractFlowStep<DamageContext> {
    public CalculateTotalDamageStep() {
        super("calculate_total_damage", 100, FlowPhase.CORE);
    }

    @Override
    public StepResult execute(DamageContext context) {
        int total = 0;

        for (DamageComponent damage : context.getComponents()) {
            total += damage.damage();
        }

        context.setTotalDamage(total);
        return StepResult.CONTINUE;
    }
}
