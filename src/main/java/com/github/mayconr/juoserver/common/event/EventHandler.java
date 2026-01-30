package com.github.mayconr.juoserver.common.event;

@FunctionalInterface
public interface EventHandler<T> {

    void handle(T event);
}
