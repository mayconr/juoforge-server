package com.github.mayconr.juoserver.standard.ai.npc.banker;

import com.github.mayconr.juoserver.game.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.ai.behavior.Behaviors;
import com.github.mayconr.juoserver.game.ai.profile.BehaviorProfile;

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
