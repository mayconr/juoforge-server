package com.github.mayconr.juoserver.infrastructure.flow;

public final class FlowDescriptor {

    private FlowDescriptor() {
    }

    public static <T extends FlowContext> String describe(String name, Flow<T> flow) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(name);
        sb.append(" pipeline steps:\n");
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-6s | %-5s | %s%n", "PHASE", "ORDER", "STEP"));
        sb.append("------------------------------------------------\n");

        for (FlowStep<T> step : flow.steps()) {
            sb.append(String.format(
                    "%-6s | %-5d | %s%n",
                    step.phase(),
                    step.order(),
                    step.name()
            ));
        }

        sb.append("------------------------------------------------");

        return sb.toString();
    }

}
