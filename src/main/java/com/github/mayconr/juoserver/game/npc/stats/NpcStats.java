package com.github.mayconr.juoserver.game.npc.stats;

/** Fully resolved attributes used to create a new NPC. */
public record NpcStats(int strength, int dexterity, int intelligence,
                       int maxHitpoints, int maxStamina, int maxMana) {
    public NpcStats {
        requirePositive("strength", strength);
        requirePositive("dexterity", dexterity);
        requirePositive("intelligence", intelligence);
        requirePositive("maxHitpoints", maxHitpoints);
        requirePositive("maxStamina", maxStamina);
        requirePositive("maxMana", maxMana);
    }

    private static void requirePositive(String field, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
    }
}
