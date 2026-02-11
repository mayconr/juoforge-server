package com.github.mayconr.juoserver.game.npc.profile;

import com.github.mayconr.juoserver.game.npc.banker.BankerBehaviorProfile;

public class BehaviorProfileFactory {

    public BehaviorProfile create(String id) {
        return new BankerBehaviorProfile();
    }

}
