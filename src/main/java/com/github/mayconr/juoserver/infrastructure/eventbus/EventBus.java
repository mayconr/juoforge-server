package com.github.mayconr.juoserver.infrastructure.eventbus;

import java.util.function.Predicate;

public interface EventBus {
    <T extends GameEvent> void register(EventRegistry<T> registry);

    <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener);

    <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener, Predicate<T> predicate);

    <T extends GameEvent> void unregister(Class<T> type, EventHandler<T> listener);

    /** Logs listener runtime failures and continues dispatching to the remaining listeners. */
    <T extends GameEvent> void publish(T event);
}
