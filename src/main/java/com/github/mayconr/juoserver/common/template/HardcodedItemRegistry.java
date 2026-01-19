package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.ItemType;
import com.github.mayconr.juoserver.game.model.Layer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HardcodedItemRegistry implements ItemTemplateRegistry {

    private static final Map<String, ItemTemplate> TEMPLATES = new HashMap<>();

    static {
        TEMPLATES.put("horse", new ItemTemplate("horse", "Horse", ItemType.MOUNT, Layer.MOUNT, 0x3EA0, true, 0, Collections.emptyMap()));
        TEMPLATES.put("spear", new ItemTemplate("spear", "a Spear", ItemType.WEAPON, Layer.ONE_HANDED, 0x0F63, true, 0, Collections.emptyMap()));
        TEMPLATES.put("robe", new ItemTemplate("robe", "a Robe", ItemType.CLOTHING, Layer.OUTER_TORSO ,0x1EFF, true, 0, Collections.emptyMap()));
    }

    @Override
    public ItemTemplate get(String name) {
        return TEMPLATES.get(name);
    }
}
