package com.github.mayconr.juoserver.game.core.prototype;

import java.util.HashMap;
import java.util.Map;

import com.github.mayconr.juoserver.game.core.model.*;

import lombok.Data;

@Data
public class NpcPrototype {
    private NpcType type;
    private int modelId;
    private String name;
    private String displayName;
    private Notoriety notoriety;
    private Race race;
    private Gender gender;
    private int hue;
    private int speechFont;
    private int speechHue;
    private int maxHitpoints;
    private int maxStamina;
    private int maxMana;
    private String ai;
    private Map<Layer, String> equippedItems = new HashMap<>();
    private MountTypePrototype mount;

    @Data
    public static class MountTypePrototype {
        private String name;
    }
}
