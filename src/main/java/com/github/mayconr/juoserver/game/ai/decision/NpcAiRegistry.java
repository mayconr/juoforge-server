package com.github.mayconr.juoserver.game.ai.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcAiRegistry {

    private final Map<String, NpcAI> npcAIMap = new HashMap<>();

    public NpcAiRegistry(List<NpcAI> npcAIList) {
        for (NpcAI npcAI : npcAIList) {
            npcAIMap.put(npcAI.getKey(), npcAI);
        }
    }

    public NpcAI get(String name) {
        return npcAIMap.get(name);
    }

}
