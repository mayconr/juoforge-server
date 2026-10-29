package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;

import java.util.Objects;

/** Server parameters supplied in code by the shard, independently of content files. */
public record ServerSettings(int ticksPerSecond, double aiUpdateIntervalSeconds, String dataFileRoot) {
    public static final int DEFAULT_TICKS_PER_SECOND = 20;
    public static final double DEFAULT_AI_UPDATE_INTERVAL_SECONDS = 0.2;

    /** Uses default timing; the shard must always supply its client data directory. */
    public ServerSettings(String dataFileRoot) {
        this(DEFAULT_TICKS_PER_SECOND, DEFAULT_AI_UPDATE_INTERVAL_SECONDS, dataFileRoot);
    }

    public ServerSettings withTicksPerSecond(int ticksPerSecond) {
        return new ServerSettings(ticksPerSecond, aiUpdateIntervalSeconds, dataFileRoot);
    }

    public ServerSettings withAiUpdateIntervalSeconds(double aiUpdateIntervalSeconds) {
        return new ServerSettings(ticksPerSecond, aiUpdateIntervalSeconds, dataFileRoot);
    }

    public ServerSettings {
        if (ticksPerSecond <= 0) {
            throw new IllegalArgumentException("ticksPerSecond must be positive");
        }
        new GamePlaySettings.Ai(aiUpdateIntervalSeconds);
        Objects.requireNonNull(dataFileRoot, "Data file root is required");
        if (dataFileRoot.isBlank()) {
            throw new IllegalArgumentException("Data file root must not be blank");
        }
    }

    GamePlaySettings applyTo(GamePlaySettings gameplay) {
        Objects.requireNonNull(gameplay, "Gameplay settings are required");
        return new GamePlaySettings(gameplay.name(), gameplay.vitals(),
                new GamePlaySettings.GameLoop(ticksPerSecond), gameplay.skills(),
                gameplay.mobile(), gameplay.world(), new GamePlaySettings.Files(dataFileRoot),
                gameplay.client(), gameplay.economy(), new GamePlaySettings.Ai(aiUpdateIntervalSeconds));
    }
}
