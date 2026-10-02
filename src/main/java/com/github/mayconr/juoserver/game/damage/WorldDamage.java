package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.model.DamageRequest;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOMobile;

public interface WorldDamage {
    void applyDamage(DamageRequest request);

    void kill(UOMobile target, UOMobile requestedBy, DamageSourceKind kind);
}
