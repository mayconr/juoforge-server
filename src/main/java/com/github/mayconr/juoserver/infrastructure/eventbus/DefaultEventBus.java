package com.github.mayconr.juoserver.infrastructure.eventbus;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultEventBus implements EventBus {

    private final Map<Class<?>, List<ConditionalHandler<?>>> listeners = new ConcurrentHashMap<>();

    @Override
    public <T extends GameEvent> void register(EventRegistry<T> registry) {
        register(registry.getType(), registry, registry.getPredicate());
    }

    @Override
    public <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener) {
        register(type, listener, e -> true);
    }

    @Override
    public <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener, Predicate<T> predicate) {
        listeners.compute(type, (key, handlers) -> {
            List<ConditionalHandler<?>> result = handlers == null ? new CopyOnWriteArrayList<>() : handlers;
            result.add(new ConditionalHandler<>(listener, predicate));
            return result;
        });
    }

    @Override
    public <T extends GameEvent> void unregister(Class<T> type, EventHandler<T> listener) {
        listeners.computeIfPresent(type, (key, handlers) -> {
            handlers.removeIf(h -> h.eventHandler.equals(listener));
            return handlers.isEmpty() ? null : handlers;
        });
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
