package com.github.mayconr.juoserver.infrastructure.eventbus;

@FunctionalInterface
public interface EventHandler<T> {

    void handle(T event);
}
