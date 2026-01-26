package com.github.mayconr.juoserver.common.policy;

public class ActionPolicyService {
    private final ActionPolicyRegistry registry;

    public ActionPolicyService(ActionPolicyRegistry registry) {
        this.registry = registry;
    }

    public PolicyResult evaluate(PolicyActions action, ActionContext ctx) {
        for (ActionPolicy<PolicyActions> policy : registry.policiesFor(action)) {
            var result = policy.evaluate(action, ctx);
            if (!result.allowed()) {
                return result;
            }
        }
        return PolicyResult.allow();
    }
}
