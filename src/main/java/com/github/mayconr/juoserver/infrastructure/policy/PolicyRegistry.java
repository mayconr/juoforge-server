package com.github.mayconr.juoserver.infrastructure.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PolicyRegistry {

    private final Map<Class<? extends ActionPolicy>, List<Policy<?>>> policies = new HashMap<>();

    public <T extends ActionPolicy> void register(Class<T> clazz, Policy<T> policy) {
        policies.computeIfAbsent(clazz, k -> new ArrayList<>())
            .add(policy);
    }

    @SuppressWarnings("unchecked")
    public <T extends ActionPolicy> List<Policy<T>> policiesFor(Class<T> actionType) {
        return (List<Policy<T>>) (List<?>)
                policies.getOrDefault(actionType, List.of());
    }
}
