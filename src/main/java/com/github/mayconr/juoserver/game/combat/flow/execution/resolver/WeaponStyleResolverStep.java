package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WeaponStyleResolverStep extends AbstractFlowStep<CombatExecutionContext> {

    public WeaponStyleResolverStep() {
        super("CombatTypeResolverStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var template = context.getWeapon().getTemplate();
        context.setStyle(template.weapon().style());
        context.setMinDamage(template.weapon().baseDamage().min());
        context.setMaxDamage(template.weapon().baseDamage().max());
        return StepResult.success();
    }
}
