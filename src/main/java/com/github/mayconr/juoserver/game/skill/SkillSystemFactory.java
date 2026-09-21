package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;

/** Creates a skill system using the services of the world being initialized. */
@FunctionalInterface
public interface SkillSystemFactory {
    SkillSystem create(GamePlaySettings settings, RNG rng, EventBus eventBus);
}
