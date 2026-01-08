package com.github.mayconr.juoserver.game.core.event;

@FunctionalInterface
public interface EventHandler<T> {

    HandlerResult handle(T event);
}
