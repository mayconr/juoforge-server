package com.github.mayconr.juoserver.game.combat.flow.execution.notify;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.model.event.CombatOccurring;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BroadcastAttackAnimationStep extends AbstractFlowStep<CombatExecutionContext> {

    private final EventBus eventBus;

    public BroadcastAttackAnimationStep(EventBus eventBus) {
        super("BroadcastAttackAnimationStep");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        log.info(context.toString());

        final var attacker = context.getSession().getAttacker();
        final var target = context.getSession().getTarget();
        final var weapon = context.getWeapon();
        final var type = switch (context.getCombatType()) {
            case WRESTLING -> new CombatOccurring.WrestlingType();
            case MELEE -> new CombatOccurring.MeleeType(weapon);
            case RANGED -> new CombatOccurring.RangedType(weapon);
            case SPELL -> new CombatOccurring.SpellType(weapon);
        };
        eventBus.publish(new CombatOccurring(attacker, target, context.getHitFrame(), type));

        return StepResult.success();
    }
}
