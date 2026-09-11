package com.github.mayconr.juoserver.game.combat.flow.preparation.build;

import com.github.mayconr.juoserver.game.combat.CombatSession;
import com.github.mayconr.juoserver.game.combat.flow.preparation.CombatPreparationContext;
import com.github.mayconr.juoserver.game.model.event.CombatStarted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.UUID;

public class BuildSessionStep extends AbstractFlowStep<CombatPreparationContext> {

    private final EventBus eventBus;

    public BuildSessionStep(EventBus eventBus) {
        super("BuildSessionStep");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(CombatPreparationContext context) {

        context.setSession(new CombatSession(UUID.randomUUID(), context.getAttacker(), context.getTargetMobile(), new CombatSession.PhysicalTrigger()));
        eventBus.publish(new CombatStarted(context.getAttacker(), context.getTargetMobile()));

        return StepResult.success();
    }
}
