package com.github.mayconr.juoserver.game.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BehaviorProfileRegistry {

    private final Map<String, BehaviorProfile> behaviorProfileMap = new HashMap<>();

    public BehaviorProfileRegistry(List<BehaviorProfile> profiles) {
        for (BehaviorProfile profile : profiles) {
            behaviorProfileMap.put(profile.getKey(), profile);
        }
    }

    public void register(String name, BehaviorProfile behaviorProfile) {
        behaviorProfileMap.put(name, behaviorProfile);
    }

    public BehaviorProfile get(String id) {
        return behaviorProfileMap.get(id);
    }

}
