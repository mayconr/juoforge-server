package com.github.mayconr.juoserver.game.combat.flow.execution.resolver;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

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
            case CombatSession.PhysicalTrigger physical -> context.getWeaponStyle().getCombatType();
            case CombatSession.SpellTrigger spell -> CombatType.SPELL;
        };

        context.setCombatType(type);

        return StepResult.success();
    }
}
