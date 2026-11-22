package com.github.mayconr.juoserver.game.damage.shared;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.DamageComponent;
import com.github.mayconr.juoserver.game.model.DamageType;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.flow.FlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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
        var target = Objects.requireNonNull(request.target(), "Damage Target is null");

        var weaponDamage = rollWeaponDamage(request.weapon());
        var attackerStr = attacker.getStrength();
        var damage = attackerStr / 10 + weaponDamage;

        context.addDamage(new DamageComponent(DamageType.PHYSICAL, damage));
        return StepResult.success();
    }

    private static int rollWeaponDamage(UOItem weapon) {
        if (weapon == null) {
            return 0;
        }
        int min = weapon.getMinDamage();
        int max = weapon.getMaxDamage();
        if (min != 0 || max != 0) {
            return rollDamage(min, max);
        }
        return Optional.ofNullable(weapon.getTemplate())
                .map(ItemTemplate::weapon)
                .map(ItemTemplate.Weapon::baseDamage)
                .map(range -> rollDamage(range.min(), range.max()))
                .orElse(0);
    }

    private static int rollDamage(int min, int max) {
        return min == max ? min : ThreadLocalRandom.current().nextInt(min, max);
    }
}
