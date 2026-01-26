package com.github.mayconr.juoserver.common.template;

import com.github.mayconr.juoserver.game.model.ItemType;
import com.github.mayconr.juoserver.game.model.Layer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HardcodedItemRegistry implements ItemTemplateRegistry {

    private static final Map<String, ItemTemplate> TEMPLATES = new HashMap<>();

    static {
        TEMPLATES.put("horse", new ItemTemplate("horse", "Horse", ItemType.MOUNT, Layer.MOUNT, 0x3EA0, true, 0, "horse",Collections.emptyMap()));
        TEMPLATES.put("spear", new ItemTemplate("spear", "a Spear", ItemType.WEAPON, Layer.TWO_HANDED, 0x0F63, true, 0, null, Collections.emptyMap()));
        TEMPLATES.put("robe", new ItemTemplate("robe", "a Robe", ItemType.CLOTHING, Layer.OUTER_TORSO ,0x1EFF, true, 0, null, Collections.emptyMap()));
        TEMPLATES.put("backpack", new ItemTemplate("backpack", "Backpack", ItemType.CONTAINER, Layer.BACKPACK, 0x0E75, false, 0, null, Map.of("gumpId", 0x003C)));
        TEMPLATES.put("sword", new ItemTemplate("sword", "a sword", ItemType.WEAPON, Layer.ONE_HANDED, 0x020E, true, 0, null, Collections.emptyMap()));
        TEMPLATES.put("hat", new ItemTemplate("hat", "hat", ItemType.CLOTHING, Layer.HEAD, 0x1713, true, 0, null, Collections.emptyMap()));
    }

    @Override
    public ItemTemplate get(String name) {
        return TEMPLATES.get(name);
    }
}
