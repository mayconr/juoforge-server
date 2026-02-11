package com.github.mayconr.juoserver.game.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultEventBus implements EventBus {

    private final Map<Class<?>, List<ConditionalHandler<?>>> listeners = new HashMap<>();

    @Override
    public <T extends GameEvent> void register(EventRegistry<T> registry) {
        register(registry.getType(), registry, registry.getPredicate());
    }

    @Override
    public <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener) {
        listeners
                .computeIfAbsent(type, k -> new ArrayList<>())
                .add(new ConditionalHandler<>(listener, e -> true));
    }

    @Override
    public <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener, Predicate<T> predicate) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>())
                .add(new ConditionalHandler<>(listener, predicate));
    }

    @Override
    public <T extends GameEvent> void unregister(Class<T> type, EventHandler<T> listener) {
        List<ConditionalHandler<?>> handlers = listeners.get(type);
        if (handlers == null) {
            return;
        }

        handlers.removeIf(h -> h.eventHandler.equals(listener));

        if (handlers.isEmpty()) {
            listeners.remove(type);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends GameEvent> void publish(T event) {
        List<ConditionalHandler<?>> handlers = listeners.get(event.getClass());
        if (handlers != null) {
            for (ConditionalHandler<?> h : handlers) {
                ConditionalHandler<T> handler = (ConditionalHandler<T>) h;
                if (handler.predicate.test(event)) {
                    handler.eventHandler.handle(event);
                }
            }
        }
    }

    private record ConditionalHandler<T>(EventHandler<T> eventHandler, Predicate<T> predicate) {}
}
