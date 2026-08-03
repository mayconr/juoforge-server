package com.github.mayconr.juoserver.game.combat.flow.execution.notify;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.event.CombatOccurring;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class BroadcastAttackAnimationStep extends AbstractFlowStep<CombatExecutionContext> {

    private EventBus eventBus;

    public BroadcastAttackAnimationStep(EventBus eventBus) {
        super("BroadcastAttackAnimationStep");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var attacker = context.getSession().getAttacker();
        final var target = context.getSession().getTarget();
        eventBus.publish(new CombatOccurring(attacker, target, context.getHitFrame(), context.getStyle()));
        return StepResult.success();
    }
}
