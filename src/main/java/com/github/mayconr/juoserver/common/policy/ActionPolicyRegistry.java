package com.github.mayconr.juoserver.common.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ActionPolicyRegistry {

    private final Map<PolicyActions, List<ActionPolicy<?>>> policies = new HashMap<>();

    public void register(PolicyActions actions, ActionPolicy<PolicyActions> policy) {
        policies.computeIfAbsent(actions, k -> new ArrayList<>())
            .add(policy);
    }

    @SuppressWarnings("unchecked")
    public List<ActionPolicy<PolicyActions>> policiesFor(PolicyActions actionType) {
        return (List<ActionPolicy<PolicyActions>>) (List<?>)
                policies.getOrDefault(actionType, List.of());
    }
}
