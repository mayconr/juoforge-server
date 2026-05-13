package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.world.context.ModuleContext;

public interface WorldModule {

    default void initialize(ModuleContext context) {}

    default void update(double delta) {};

}
