package com.github.mayconr.juoserver.game.combat;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class CombatSessionRegistryImpl implements CombatSessionRegistry {

    private final Map<UUID, CombatSession> sessionsByID = new ConcurrentHashMap<>();
    private final Map<Integer, CombatSession> sessionsBySerialId = new ConcurrentHashMap<>();

    @Override
    public CombatSession getById(UUID id) {
        return sessionsByID.get(id);
    }

    @Override
    public CombatSession getByPlayer(UOMobile mobile) {
        return sessionsBySerialId.get(mobile.getSerialId());
    }

    @Override
    public void register(CombatSession session) {
        unregister(session.getAttacker());
        sessionsByID.put(session.getId(), session);
        sessionsBySerialId.put(session.getAttacker().getSerialId(), session);
    }

    @Override
    public void unregister(UOMobile mobile) {
        var session = sessionsBySerialId.remove(mobile.getSerialId());
        if (session != null) {
            session.close();
            sessionsByID.remove(session.getId());
        }
    }

    @Override
    public void unregister(CombatSession session) {
        session.close();
        sessionsByID.remove(session.getId());
        sessionsBySerialId.remove(session.getAttacker().getSerialId(), session);
    }

    @Override
    public Collection<CombatSession> getSessions() {
        return sessionsByID.values();
    }
}
