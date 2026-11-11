package com.github.mayconr.juoserver.game.combat.progression;

import com.github.mayconr.juoserver.game.combat.CombatWeaponSkillResolver;
import com.github.mayconr.juoserver.game.skill.SkillGainAttempt;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import java.util.List;
import java.util.Optional;

/**
 * Attempts the attacker's weapon skill and Tactics on every resolved swing,
 * whether it hits or misses. Training intensity is the defender's effective weapon skill,
 * with a minimum of 1.0, including when the defender has no skills.
 * Replace this policy to change eligibility, beneficiaries, skills or training intensity.
 */
public class DefaultCombatSkillGainPolicy implements CombatSkillGainPolicy {
    private static final int TACTICS = 27;
    private static final double MIN_DIFFICULTY = 25;

    @Override
    public List<SkillGainAttempt> resolve(CombatSkillGainContext context) {
        var attacker = context.attacker();
        double trainingIntensity = Optional.ofNullable(context.defender().getSkills())
                .map(skills -> skills.get(CombatWeaponSkillResolver.forStyle(context.defenderStyle())))
                .map(SkillValue::getValue)
                .map(value -> Math.max(MIN_DIFFICULTY, value))
                .orElse(MIN_DIFFICULTY);

        var gainContext = SkillGainContext.empty();
        return List.of(
                new SkillGainAttempt(attacker, CombatWeaponSkillResolver.forStyle(context.attackerStyle()), trainingIntensity, gainContext),
                new SkillGainAttempt(attacker, TACTICS, trainingIntensity, gainContext)
        );
    }
}
