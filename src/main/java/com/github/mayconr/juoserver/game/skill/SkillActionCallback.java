package com.github.mayconr.juoserver.game.skill;

/** Callback executed on the game loop with the one-based number of swings executed. */
@FunctionalInterface
public interface SkillActionCallback {
    void execute(int swingsExecuted);
}
