package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.Notoriety;
import com.github.mayconr.juoserver.game.model.NpcType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HardcodedNpcTemplateLoader implements NpcTemplateRegistry {

    private static final Map<String, NpcTemplate> TEMPLATE = new HashMap<>();

    static {
        TEMPLATE.put("horse", new NpcTemplate("horse", "a Horse",NpcType.MOUNT, 0xc8,"BANKER", Notoriety.GREY_ANIMAL, 0, "horse",Collections.emptyMap()));
        TEMPLATE.put("banker", new NpcTemplate("banker", "Gary the banker", NpcType.HUMAN, 0x190, "BANKER", Notoriety.INNOCENT, 0x83EA, null, Collections.emptyMap()));
    }

    @Override
    public NpcTemplate get(String name) {
        return TEMPLATE.get(name);
    }
}
