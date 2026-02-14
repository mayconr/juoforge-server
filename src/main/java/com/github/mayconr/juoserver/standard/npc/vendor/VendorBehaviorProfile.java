package com.github.mayconr.juoserver.standard.npc.vendor;

import com.github.mayconr.juoserver.game.npc.behavior.IdleBehavior;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.npc.behavior.TalkBehavior;
import com.github.mayconr.juoserver.game.npc.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public class VendorBehaviorProfile implements BehaviorProfile {

    private final IdleBehavior idleBehavior = new IdleBehavior();
    private final TalkBehavior talkBehavior = new TalkBehavior();
    private final VendorBehavior vendorBehavior;

    public VendorBehaviorProfile(WorldInternal world) {
        this.vendorBehavior = new VendorBehavior(world);
    }

    @Override
    public NpcBehavior idle() {
        return idleBehavior;
    }

    @Override
    public NpcBehavior talk() {
        return talkBehavior;
    }

    @Override
    public NpcBehavior service() {
        return vendorBehavior;
    }
}
