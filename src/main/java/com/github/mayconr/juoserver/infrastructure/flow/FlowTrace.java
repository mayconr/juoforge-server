package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class FlowTrace {
    private final List<TraceEntry> entries = new ArrayList<>();
    private final Deque<String> groupStack = new ArrayDeque<>();

    public void enterGroup(String name) {
        groupStack.push(name);
    }

    public void exitGroup(String name) {
        groupStack.pop();
    }

    public void logStep(String stepName, long duration, String status) {
        entries.add(new TraceEntry(List.copyOf(groupStack), stepName, duration, status));
    }

    public List<TraceEntry> entries() {
        return entries;
    }

    public TraceEntry lastEntry() {
        if (entries.isEmpty()) {
            return null;
        }
        return entries.getLast();
    }
}
