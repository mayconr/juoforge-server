package com.github.mayconr.juoserver.game.combat.flow.preparation.build;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.UUID;

public class BuildSessionStep extends AbstractFlowStep<CombatPreparationContext> {
    public BuildSessionStep() {
        super("BuildSessionStep");
    }

    @Override
    public StepResult execute(CombatPreparationContext context) {

        context.setSession(new CombatSession(UUID.randomUUID(), context.getAttacker(), context.getTargetMobile()));

        return StepResult.success();
    }
}
