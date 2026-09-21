package com.github.mayconr.juoserver.game.combat.progression;

import com.github.mayconr.juoserver.game.skill.SkillGainAttempt;

import java.util.List;

/**
 * Selects skill gain attempts for a resolved swing, including misses.
 * Implementations should return a non-null list without applying gains themselves.
 * Return an empty list to disable gains. Each entry is attempted once, in order.
 */
@FunctionalInterface
public interface CombatSkillGainPolicy {
    List<SkillGainAttempt> resolve(CombatSkillGainContext context);
}
