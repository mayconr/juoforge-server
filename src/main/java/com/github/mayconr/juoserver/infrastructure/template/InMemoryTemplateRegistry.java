package com.github.mayconr.juoserver.infrastructure.template;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemoryTemplateRegistry<K, T> implements TemplateRegistry<K, T> {

    private final List<T> templates;
    private final Map<K, List<T>> indexed;

    public InMemoryTemplateRegistry(Collection<T> templates, Function<T, K> keyExtractor) {
        this.templates = List.copyOf(templates);
        this.indexed = templates.stream()
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                List::copyOf
                        )
                ));
    }

    @Override
    public List<T> get(K key) {
        return indexed.getOrDefault(key, List.of());
    }

    @Override
    public List<T> all() {
        return templates;
    }

    @Override
    public List<T> find(Predicate<T> predicate) {
        return templates
                .stream()
                .filter(predicate)
                .toList();
    }
}
