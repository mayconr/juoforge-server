package com.github.mayconr.juoserver.game.session.npc.impl.banker;

import com.github.mayconr.juoserver.game.session.npc.profile.BehaviorProfile;
import com.github.mayconr.juoserver.game.session.npc.behavior.IdleBehavior;
import com.github.mayconr.juoserver.game.session.npc.behavior.NpcBehavior;
import com.github.mayconr.juoserver.game.session.npc.behavior.TalkBehavior;

public class BankerBehaviorProfile implements BehaviorProfile {

    private final IdleBehavior idleBehavior = new IdleBehavior();
    private final TalkBehavior talkBehavior = new TalkBehavior();
    private final BankServiceBehavior bankServiceBehavior = new BankServiceBehavior();

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
        return bankServiceBehavior;
    }
}
