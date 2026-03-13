package com.github.mayconr.juoserver.game.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcAiRegistry {

    private final Map<String, AI> npcAIMap = new HashMap<>();

    public NpcAiRegistry(List<AI> AIList) {
        for (AI AI : AIList) {
            npcAIMap.put(AI.getKey(), AI);
        }
    }

    public AI get(String name) {
        return npcAIMap.get(name);
    }

}
