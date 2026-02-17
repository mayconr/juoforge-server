package com.github.mayconr.juoserver.standard.ai.animal;

import com.github.mayconr.juoserver.game.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.ai.behavior.Behaviors;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfile;

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
