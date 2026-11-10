package com.github.mayconr.juoserver.game.skill;

/** Game-loop callback receiving the one-based count of swings executed by the action. */
@FunctionalInterface
public interface SkillCallback {
    void execute(int executions);
}
