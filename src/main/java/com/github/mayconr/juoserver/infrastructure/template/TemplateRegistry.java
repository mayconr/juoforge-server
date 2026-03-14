package com.github.mayconr.juoserver.infrastructure.template;

import java.util.List;

public interface TemplateRegistry<K, T> {
    List<T> get(K key);
    List<T> all();
    List<T> find(java.util.function.Predicate<T> predicate);
}
