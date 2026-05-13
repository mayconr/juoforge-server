package com.github.mayconr.juoserver.infrastructure.flow;

import java.util.List;

public record TraceEntry(
        List<String> groups,
        String step,
        long durationMs,
        String status
) {}
