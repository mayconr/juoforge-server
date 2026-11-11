package com.github.mayconr.juoserver.game.temporal;

/** Persisted state of the game clock: game seconds elapsed since the calendar epoch. */
public record GameClockState(long epochSeconds) {

    public GameClockState {
        if (epochSeconds < 0) {
            throw new IllegalArgumentException("Epoch seconds cannot be negative");
        }
    }
}
