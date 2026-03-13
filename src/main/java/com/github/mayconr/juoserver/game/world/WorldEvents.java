package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventRegistry;
import com.github.mayconr.juoserver.infrastructure.eventbus.GameEvent;

import java.util.function.Predicate;

public interface WorldEvents {

    <T extends GameEvent> void on(Class<T> type, EventHandler<T> listener);

    <T extends GameEvent> void on(Class<T> type, EventHandler<T> listener, Predicate<T> predicate);

    <T extends GameEvent> void on(EventRegistry<T> registry);

}
