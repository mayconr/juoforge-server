package com.github.mayconr.juoserver.standard.npc.banker;

import com.github.mayconr.juoserver.game.world.module.ai.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.world.module.ai.behavior.IdleBehavior;
import com.github.mayconr.juoserver.game.world.module.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.world.module.ai.behavior.TalkBehavior;

public class BankerBehaviorProfile implements BehaviorProfile {

    private final IdleBehavior idleBehavior = new IdleBehavior();
    private final TalkBehavior talkBehavior = new TalkBehavior();
    private final BankBehavior bankBehavior = new BankBehavior();

    @Override
    public String getKey() {
        return "BANKER";
    }

    @Override
    public Behavior idle() {
        return idleBehavior;
    }

    @Override
    public Behavior talk() {
        return talkBehavior;
    }

    @Override
    public Behavior service() {
        return bankBehavior;
    }
}
