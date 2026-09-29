package com.github.mayconr.juoserver.game.mobile.template;

import com.github.mayconr.juoserver.game.npc.stats.NpcStats;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

/** A complete stat profile; profiles cannot inherit from other profiles. */
public record NpcStatProfile(String name, int strength, int dexterity, int intelligence,
                             int maxHitpoints, int maxStamina, int maxMana) implements BaseTemplate {
    public NpcStatProfile {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("NPC stat profile name is required");
        }
        try {
            new NpcStats(strength, dexterity, intelligence, maxHitpoints, maxStamina, maxMana);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("NPC stat profile '" + name + "': " + e.getMessage(), e);
        }
    }
}
