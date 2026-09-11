package com.github.mayconr.juoserver.game.combat.flow.execution.notify;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.event.CombatOccurring;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BroadcastAttackAnimationStep extends AbstractFlowStep<CombatExecutionContext> {

    private EventBus eventBus;

    public BroadcastAttackAnimationStep(EventBus eventBus) {
        super("BroadcastAttackAnimationStep");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        log.info(context.toString());

        final var attacker = context.getSession().getAttacker();
        final var target = context.getSession().getTarget();
        eventBus.publish(new CombatOccurring(attacker, target, context.getHitFrame(), context.getCombatType()));

        return StepResult.success();
    }
}
