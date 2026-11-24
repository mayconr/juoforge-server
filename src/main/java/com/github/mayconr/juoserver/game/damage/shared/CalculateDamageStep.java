package com.github.mayconr.juoserver.game.damage.shared;

import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.Objects;

public class CalculateDamageStep<T extends CalculateDamageContext> implements FlowStep<T> {

    @Override
    public String name() {
        return "DamageCalculationStep";
    }

    @Override
    public StepResult execute(T context) {
        var request = Objects.requireNonNull(context.damageRequest(), "Damage request is null");
        var combatType = Objects.requireNonNull(request.combatType(), "Combat type is null");
        var attacker = Objects.requireNonNull(request.attacker(), "Damage Attacker is null");
        Objects.requireNonNull(request.target(), "Damage Target is null");

        int damage = AttackDamageCalculator.roll(attacker, request.weapon(), combatType);
        context.addDamage(new DamageComponent(DamageType.PHYSICAL, damage));
        return StepResult.success();
    }
}
