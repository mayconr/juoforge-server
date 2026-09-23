package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Documented scroll offsets for the New Spellbook extended command (0x1B).
 */
@Getter
@RequiredArgsConstructor
public enum SpellbookType {
    MAGERY(1),
    NECROMANCY(101),
    CHIVALRY(201),
    BUSHIDO(401),
    NINJITSU(501),
    SPELLWEAVING(601);

    private final int scrollOffset;
}
