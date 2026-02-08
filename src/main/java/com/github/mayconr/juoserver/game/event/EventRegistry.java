package com.github.mayconr.juoserver.game.event;

import java.util.function.Predicate;

public interface EventRegistry<T> extends EventHandler<T> {
    Class<T> getType();

    Predicate<T> getPredicate();
}
