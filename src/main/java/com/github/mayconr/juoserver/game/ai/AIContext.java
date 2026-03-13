package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.ai.actions.NpcAction;

public interface AIContext {
    World world();
    UONpc npc();
    <T> void set(String key, T value);
    <T> T get(String key, Class<T> type);
    boolean has(String key);
    void remove(String key);
    void enqueue(NpcAction action);
}
