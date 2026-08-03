package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CombatTypeResolverStep extends AbstractFlowStep<CombatExecutionContext> {

    public CombatTypeResolverStep() {
        super("CombatTypeResolverStep");
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var session = context.getSession();
        final var trigger = session.getTrigger();

        var type = switch (trigger) {
            case CombatSession.PhysicalTrigger physical -> {
                if (context.getWeapon() == null) {
                    yield CombatType.WRESTLING;
                }
                final var weapon = context.getWeapon().getTemplate().weapon();

                yield weapon.style().getCombatType();
            }
            case CombatSession.SpellTrigger spell -> CombatType.SPELL;
        };

        System.out.println(type);

        return StepResult.success();
    }
}
