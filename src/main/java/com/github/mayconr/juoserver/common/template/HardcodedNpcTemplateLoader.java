package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;

import java.util.HashMap;
import java.util.Map;

public class HardcodedNpcTemplateLoader implements NpcTemplateRegistry {

    private static final Map<String, NpcTemplate> TEMPLATE = new HashMap<>();

    static {
        TEMPLATE.put("horse", new NpcTemplate("horse", NpcType.MOUNT, 0xc8,"BANKER", Notoriety.GREY_ANIMAL, 0, Map.of("mount", "horse")));
    }

    @Override
    public NpcTemplate get(String name) {
        return TEMPLATE.get(name);
    }
}
