package com.github.mayconr.juoserver.common.policy;

public sealed interface PolicyResult
        permits PolicyResult.Allowed, PolicyResult.Denied {

    boolean allowed();
    String reason();

    record Allowed() implements PolicyResult {
        @Override public boolean allowed() { return true; }

        @Override
        public String reason() {
            return "";
        }
    }

    record Denied(String reason) implements PolicyResult {
        @Override public boolean allowed() { return false; }

    }

    static PolicyResult allow() {
        return new Allowed();
    }

    static PolicyResult deny(String reason) {
        return new Denied(reason);
    }
}
