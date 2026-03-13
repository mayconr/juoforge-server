package com.github.mayconr.juoserver.game.ai.profiles;

import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.behaviors.Behaviors;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;

public class AnimalBehaviorProfile implements BehaviorProfile {

    @Override
    public String getKey() {
        return "ANIMAL";
    }

    @Override
    public Behavior idle() {
        return Behaviors.IDLE_BEHAVIOR;
    }

    @Override
    public Behavior talk() {
        return Behaviors.TALK_BEHAVIOR;
    }

    @Override
    public Behavior wander() {
        return Behaviors.WANDER_BEHAVIOR;
    }

    @Override
    public Behavior service() {
        return null;
    }
}
