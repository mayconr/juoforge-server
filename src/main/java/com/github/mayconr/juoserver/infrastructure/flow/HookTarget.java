package com.github.mayconr.juoserver.infrastructure.flow;

public sealed interface HookTarget permits AnyHookTarget, StepNameHookTarget {

    static HookTarget any() {
        return new AnyHookTarget();
    }

    static HookTarget stepName(String stepName) {
        return new StepNameHookTarget(stepName);
    }

}
