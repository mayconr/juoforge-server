package com.github.mayconr.juoserver.infrastructure.policy;

public sealed interface PolicyResult permits PolicyResult.Allowed, PolicyResult.Denied {

    boolean allowed();
    String reason();

    record Allowed(String reason) implements PolicyResult {
        @Override public boolean allowed() { return true; }
    }

    record Denied(String reason) implements PolicyResult {
        @Override public boolean allowed() { return false; }
    }

    static <T extends ActionPolicy> PolicyResult allow() {
        return new Allowed("");
    }

    static <T extends ActionPolicy> PolicyResult deny(String reason) {
        return new Denied(reason);
    }
}
