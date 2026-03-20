package com.github.mayconr.juoserver.infrastructure.template;

import java.util.List;
import java.util.Optional;

public interface TemplateRegistry<K, T> {
    List<T> get(K key);
    Optional<T> getFisrt(K key);
    List<T> all();
    List<T> find(java.util.function.Predicate<T> predicate);
}
