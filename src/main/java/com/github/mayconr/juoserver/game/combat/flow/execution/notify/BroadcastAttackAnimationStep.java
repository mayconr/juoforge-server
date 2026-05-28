package com.github.mayconr.juoserver.game.combat.flow.execution.notify;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.game.model.event.AnimationSent;
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
        var animation = AnimationOptions.simpleForward(AnimationType.SWING_SWORD_FROM_HORSE, context.getAnimFrame());
        eventBus.publish(new AnimationSent(context.getSession().getAttacker(), animation));
        return StepResult.success();
    }
}
