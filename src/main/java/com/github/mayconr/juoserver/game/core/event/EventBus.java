package com.github.mayconr.juoserver.game.core.event;

import java.util.function.Predicate;

public interface EventBus {
    <T extends GameEvent> void register(EventRegistry<T> registry);

    <T extends GameEvent> void register(Class<T> type, EventHandler<T> listener);

    <T extends GameEvent> void register(
            Class<T> type, EventHandler<T> listener, Predicate<T> predicate);



    <T extends GameEvent> HandlerResult publish(T event);
}
