package com.github.mayconr.juoserver.common.event;

@FunctionalInterface
public interface EventHandler<T> {

    HandlerResult handle(T event);
}
