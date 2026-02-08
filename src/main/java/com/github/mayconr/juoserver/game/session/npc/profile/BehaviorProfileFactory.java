package com.github.mayconr.juoserver.game.session.npc.profile;

import com.github.mayconr.juoserver.game.session.npc.impl.banker.BankerBehaviorProfile;

public class BehaviorProfileFactory {

    public BehaviorProfile create(String id) {
        return new BankerBehaviorProfile();
    }

}
