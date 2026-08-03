package com.github.mayconr.juoserver.game.combat.flow.execution.damage;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.model.DamageRequest;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplyDamageStep extends AbstractFlowStep<CombatExecutionContext> {

    private final DamageModule damageModule;

    public ApplyDamageStep(DamageModule damageModule) {
        super("ApplyCombatDamageStep");
        this.damageModule = damageModule;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        final var session = context.getSession();
        final var attacker = session.getAttacker();
        final var target = session.getTarget();
        final var kind = DamageSourceKind.MELEE;

        final var damage = DamageRequest.of(attacker, target, kind, context.getDamages());
        damageModule.applyDamage(damage);

        if (!target.isAlive()) {
            session.close();
        }
        return StepResult.success();
    }
}
