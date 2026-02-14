package com.github.mayconr.juoserver.standard.npc.banker;

import com.github.mayconr.juoserver.game.npc.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.npc.behavior.IdleBehavior;
import com.github.mayconr.juoserver.game.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.npc.behavior.TalkBehavior;

public class BankerBehaviorProfile implements BehaviorProfile {

    private final IdleBehavior idleBehavior = new IdleBehavior();
    private final TalkBehavior talkBehavior = new TalkBehavior();
    private final BankBehavior bankBehavior = new BankBehavior();

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
        return bankBehavior;
    }
}
