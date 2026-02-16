package com.github.mayconr.juoserver.infrastructure.policy;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRegistry registry;

    public <T extends ActionPolicy> PolicyResult evaluate(Class<T> action, ActionPolicy ctx) {
        for (Policy<T> policy : registry.policiesFor(action)) {
            var result = policy.evaluate((T) ctx);
            if (!result.allowed()) {
                return result;
            }
        }
        return PolicyResult.allow();
    }
}
