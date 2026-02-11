package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.action.NpcAction;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public interface NpcContext {
    WorldInternal world();
    UONpc npc();
    <T> void set(String key, T value);
    <T> T get(String key, Class<T> type);
    boolean has(String key);
    void remove(String key);
    void enqueue(NpcAction action);
}
