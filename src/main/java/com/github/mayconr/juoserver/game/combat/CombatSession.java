package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class CombatSession {
    private final UUID id;
    private final UOMobile attacker;
    private final UOMobile target;
    private final long startedAt;
    private long lastAggressionAt;
    private boolean active;

    public CombatSession(UUID id, UOMobile attacker, UOMobile target) {
        this.id = id;
        this.attacker = attacker;
        this.target = target;
        this.startedAt = System.currentTimeMillis();
        this.active = true;
    }

    public void refreshAggression() {
        this.lastAggressionAt = System.currentTimeMillis();
    }

    public void close() {
        this.active = false;
    }

}
