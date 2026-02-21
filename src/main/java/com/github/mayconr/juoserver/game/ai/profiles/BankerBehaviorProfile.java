package com.github.mayconr.juoserver.game.ai.profiles;

import com.github.mayconr.juoserver.game.ai.Behavior;
import com.github.mayconr.juoserver.game.ai.behaviors.Behaviors;
import com.github.mayconr.juoserver.game.ai.BehaviorProfile;
import com.github.mayconr.juoserver.game.ai.behaviors.BankBehavior;

public class BankerBehaviorProfile implements BehaviorProfile {

    private final BankBehavior bankBehavior = new BankBehavior();

    @Override
    public String getKey() {
        return "BANKER";
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
    public Behavior service() {
        return bankBehavior;
    }

    @Override
    public Behavior wander() {
        return Behaviors.WANDER_BEHAVIOR;
    }
}
