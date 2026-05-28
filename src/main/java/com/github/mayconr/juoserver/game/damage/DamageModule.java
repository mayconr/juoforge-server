package com.github.mayconr.juoserver.game.damage;

import com.github.mayconr.juoserver.game.model.DamageRequest;
import com.github.mayconr.juoserver.game.model.DamageSourceKind;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.WorldModule;

public interface DamageModule extends WorldModule {

    void applyDamage(DamageRequest request);

    void kill(UOMobile target, UOMobile requestedBy, DamageSourceKind kind);

}
