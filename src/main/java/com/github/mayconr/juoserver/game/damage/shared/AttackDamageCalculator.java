package com.github.mayconr.juoserver.game.damage.shared;

import com.github.mayconr.juoserver.game.combat.DamageRange;
import com.github.mayconr.juoserver.game.combat.flow.execution.CombatType;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.BaseDamage;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOMobile;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public final class AttackDamageCalculator {
    private static final double TACTICS_BONUS_PER_POINT = 0.003;
    private static final BaseDamage NO_WEAPON_DAMAGE = new BaseDamage(0, 0);

    private AttackDamageCalculator() {}

    public static int roll(UOMobile attacker, UOItem weapon, CombatType type) {
        var bounds = weaponDamage(weapon);
        int rolled = bounds.min() == bounds.max()
                ? bounds.min()
                : ThreadLocalRandom.current().nextInt(bounds.min(), bounds.max());
        return modifiers(attacker, type).apply(rolled);
    }

    public static DamageRange range(UOMobile attacker, UOItem weapon, CombatType type) {
        var bounds = weaponDamage(weapon);
        if (bounds.min() > bounds.max()) {
            throw new IllegalArgumentException("Minimum weapon damage exceeds maximum weapon damage");
        }
        int maxRoll = bounds.min() == bounds.max() ? bounds.max() : bounds.max() - 1;
        var modifiers = modifiers(attacker, type);
        return new DamageRange(modifiers.apply(bounds.min()), modifiers.apply(maxRoll));
    }

    private static BaseDamage weaponDamage(UOItem weapon) {
        if (weapon == null) {
            return NO_WEAPON_DAMAGE;
        }
        int min = weapon.getMinDamage();
        int max = weapon.getMaxDamage();
        if (min != 0 || max != 0) {
            return new BaseDamage(min, max);
        }
        return Optional.ofNullable(weapon.getTemplate())
                .map(ItemTemplate::weapon)
                .map(ItemTemplate.Weapon::baseDamage)
                .orElse(NO_WEAPON_DAMAGE);
    }

    private static DamageModifiers modifiers(UOMobile attacker, CombatType type) {
        int strengthDamage = attacker.getStrength() / 10;
        var skills = type == CombatType.SPELL ? null : attacker.getSkills();
        double tactics = skills == null ? 0.0 : skills.getEffectiveValue(SkillValue.TACTICS_ID);
        double bonus = Math.clamp(tactics, 0.0, 100.0) * TACTICS_BONUS_PER_POINT;
        return new DamageModifiers(strengthDamage, 1.0 + bonus);
    }

    private record DamageModifiers(int strengthDamage, double multiplier) {
        int apply(int weaponDamage) {
            return (int) Math.round((strengthDamage + weaponDamage) * multiplier);
        }
    }
}
