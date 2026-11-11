package com.github.mayconr.juoserver.game.temporal;

import java.util.Objects;

/** Game seconds elapsed since the calendar epoch; not thread-safe. */
public class GameClock {
    private long epochSeconds;

    public GameClock(long initialEpochSeconds) {
        this.epochSeconds = new GameClockState(initialEpochSeconds).epochSeconds();
    }

    public static GameClock restore(GameClockState state) {
        return new GameClock(Objects.requireNonNull(state, "Clock state is required").epochSeconds());
    }

    public long epochSeconds() {
        return epochSeconds;
    }

    public GameClockState state() {
        return new GameClockState(epochSeconds);
    }

    public void advance(long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds cannot be negative");
        }

        epochSeconds = Math.addExact(epochSeconds, seconds);
    }
}
