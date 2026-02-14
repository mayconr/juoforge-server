package com.github.mayconr.juoserver.game.npc.profile;

import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.standard.npc.banker.BankerBehaviorProfile;
import com.github.mayconr.juoserver.standard.npc.vendor.VendorBehaviorProfile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BehaviorProfileFactory {

    private final WorldInternal world;

    public BehaviorProfile create(String id) {
        return switch (id) {
            case "BANKER" -> new BankerBehaviorProfile();
            case "VENDOR" -> new VendorBehaviorProfile(world);
            default -> throw new IllegalArgumentException("Invalid behavior profile id: " + id);
        };
    }

}
