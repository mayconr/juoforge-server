package com.github.mayconr.juoserver.game.ai.definition;

import com.github.mayconr.juoserver.game.ai.actions.NpcAction;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;

import java.util.*;

public class AIFlowContext extends AbstractSyncFlowContext<Void> {

    private final UONpc npc;
    private final World world;

    private final Map<String, Object> data = new HashMap<>();
    private final Queue<NpcAction> actions = new ArrayDeque<>();

    private double delta;

    private final Queue<GameEvent> events = new ArrayDeque<>();

    public AIFlowContext(UONpc npc, World world) {
        this.npc = npc;
        this.world = world;
    }

    // =========================
    // Core data
    // =========================

    public UONpc npc() {
        return npc;
    }

    public World world() {
        return world;
    }

    public double delta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    // =========================
    // Key-value state
    // =========================

    public <T> void set(String key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) return null;
        return (T) value;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    // =========================
    // Actions
    // =========================

    public void enqueueAction(NpcAction action) {
        actions.add(action);
    }

    public Queue<NpcAction> actions() {
        return actions;
    }

    public void clearActions() {
        actions.clear();
    }

    // =========================
    // EVENTS
    // =========================

    public <T extends GameEvent> void enqueueEvent(T event) {
        events.add(event);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameEvent> T pollEvent(Class<T> type) {
        for (Iterator<GameEvent> it = events.iterator(); it.hasNext();) {
            Object e = it.next();
            if (type.isInstance(e)) {
                it.remove();
                return (T) e;
            }
        }
        return null;
    }

    public <T extends GameEvent> T peekEvent(Class<T> type) {
        for (GameEvent e : events) {
            if (type.isInstance(e)) {
                return type.cast(e);
            }
        }
        return null;
    }
}
