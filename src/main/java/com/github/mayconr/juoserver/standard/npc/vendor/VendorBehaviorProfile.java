package com.github.mayconr.juoserver.standard.npc.vendor;

import com.github.mayconr.juoserver.game.world.module.ai.behavior.Behavior;
import com.github.mayconr.juoserver.game.world.module.ai.behavior.IdleBehavior;
import com.github.mayconr.juoserver.game.world.module.ai.behavior.TalkBehavior;
import com.github.mayconr.juoserver.game.world.module.ai.profile.BehaviorProfile;

public class VendorBehaviorProfile implements BehaviorProfile {

    private final IdleBehavior idleBehavior = new IdleBehavior();
    private final TalkBehavior talkBehavior = new TalkBehavior();
    private final VendorBehavior vendorBehavior = new VendorBehavior();

    @Override
    public String getKey() {
        return "VENDOR";
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
        return vendorBehavior;
    }
}
