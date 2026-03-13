package com.github.mayconr.juoserver;

public record JuoforgeConfiguration(
        EngineSettings settings,
        WorldConfiguration world
) {
    public static JuoforgeConfiguration defaults() {
        return new JuoforgeConfiguration(EngineSettings.defaults(), WorldConfiguration.builder().build());
    }
}
