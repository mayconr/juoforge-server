package com.github.mayconr.juoserver.game.combat.flow.execution.calculation;

import com.github.mayconr.juoserver.game.combat.flow.execution.CombatExecutionContext;
import com.github.mayconr.juoserver.game.combat.CombatHitResult;
import com.github.mayconr.juoserver.game.combat.CombatWeaponSkillResolver;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.WeaponStyle;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

/**
 * Determines whether a combat swing hits before damage is calculated and applied.
 * Uses each mobile's effective skill for its weapon style, with Wrestling used
 * when unarmed. The attacker's style must already be resolved in the context;
 * the defender's style is resolved from equipped items.
 *
 * <p>The hit probability is calculated as follows:
 * <pre>
 * attack  = (attackerSkill + 20) * (1 + hitChanceIncrease / 100)
 * defense = (defenderSkill + 20) * (1 + defenseChanceIncrease / 100)
 * chance  = attack / (2 * defense)
 * </pre>
 * Skill values are floored at zero, each percentage bonus is clamped to [0, 45],
 * and the final probability is clamped to [0.02, 1.0]. The skill offset of 20
 * keeps zero-skill combat meaningful and prevents division by zero. Equal attack
 * and defense values produce a 50% hit chance.
 *
 * <p>The injected {@link RNG} rolls the resulting probability and stores HIT or
 * MISS in the context. Both outcomes continue to skill gain processing.
 * {@link StopOnCombatMissStep} subsequently stops misses before damage is applied,
 * without closing the combat session.
 */
public class CheckCombatHitStep extends AbstractFlowStep<CombatExecutionContext> {
    private final RealmStorage storage;
    private final RNG rng;

    public CheckCombatHitStep(RealmStorage storage, RNG rng) {
        super("CheckCombatHitStep");
        this.storage = storage;
        this.rng = rng;
    }

    @Override
    public StepResult execute(CombatExecutionContext context) {
        var attacker = context.getSession().getAttacker();
        var defender = context.getSession().getTarget();
        context.setDefenderWeaponStyle(defenderStyle(defender));
        double attack = (combatSkill(attacker, context.getWeaponStyle()) + 20.0)
                * (1.0 + Math.clamp(attacker.getHitChanceIncrease(), 0, 45) / 100.0);
        double defense = (combatSkill(defender, context.getDefenderWeaponStyle()) + 20.0)
                * (1.0 + Math.clamp(defender.getDefenseChanceIncrease(), 0, 45) / 100.0);
        double chance = Math.clamp(attack / (2.0 * defense), 0.02, 1.0);

        context.setHitResult(rng.roll(chance) ? CombatHitResult.HIT : CombatHitResult.MISS);
        return StepResult.success();
    }

    private WeaponStyle defenderStyle(UOMobile defender) {
        var equipped = defender.getEquippedItems();
        if (equipped != null) {
            for (var layer : new Layer[]{Layer.ONE_HANDED, Layer.TWO_HANDED}) {
                var serial = equipped.get(layer);
                if (serial == null) continue;
                var item = storage.getItem(serial).orElse(null);
                if (item != null && item.getTemplate() != null && item.getTemplate().weapon() != null) {
                    return item.getTemplate().weapon().style();
                }
            }
        }
        return WeaponStyle.WRESTLING;
    }

    private double combatSkill(UOMobile mobile, WeaponStyle style) {
        int skillId = CombatWeaponSkillResolver.forStyle(style);
        return Math.max(0.0, mobile.getSkills().get(skillId).getValue());
    }
}
