package com.github.mayconr.juoserver.infrastructure.flow;

public class FlowProfiler {
    public static long start() {
        return System.nanoTime();
    }

    public static long end(long start) {
        return System.nanoTime() - start;
    }
}
