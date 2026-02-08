package com.github.mayconr.juoserver.game.event;

@FunctionalInterface
public interface EventHandler<T> {

    void handle(T event);
}
