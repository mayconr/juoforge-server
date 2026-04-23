package com.github.mayconr.juoserver.infrastructure.flow;

public class FlowTraceFormatter {
    public static String format(FlowTrace trace) {
        StringBuilder sb = new StringBuilder();

        for (TraceEntry entry : trace.entries()) {
            int indent = entry.groups().size();

            sb.append("  ".repeat(indent))
                    .append("├── ")
                    .append(entry.step())
                    .append(" [")
                    .append(entry.status())
                    .append("] (")
                    .append(entry.durationMs())
                    .append("ms)")
                    .append("\n");
        }

        return sb.toString();
    }
}
