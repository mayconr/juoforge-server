package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;
import java.util.UUID;

public interface CombatSessionRegistry {

    CombatSession getById(UUID id);
    CombatSession getByPlayer(UOMobile mobile);
    void register(CombatSession session);
    void unregister(UOMobile mobile);
    void unregister(CombatSession session);
    Collection<CombatSession> getSessions();
}
