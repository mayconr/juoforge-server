package com.github.mayconr.juoserver.game.combat.flow.execution.damage;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.damage.DamageModule;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageRequest;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

        final int baseDamage = ThreadLocalRandom.current()
                .nextInt(context.getMinDamage(), context.getMaxDamage() + 1);

        final var components = List.of(new DamageComponent(DamageType.PHYSICAL, baseDamage));
        final var damage = DamageRequest.of(attacker, target, kind, components);
        damageModule.applyDamage(damage);

        if (!target.isAlive()) {
            session.close();
        }
        return StepResult.success();
    }
}
