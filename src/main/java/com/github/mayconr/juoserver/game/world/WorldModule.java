package com.github.mayconr.juoserver.game.world;

public interface WorldModule {

    default void initialize(ModuleContext context) {}

    default void update(double delta) {};

}
