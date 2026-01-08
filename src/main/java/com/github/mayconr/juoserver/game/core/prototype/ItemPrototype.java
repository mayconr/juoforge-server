package com.github.mayconr.juoserver.game.core.prototype;

import com.github.mayconr.juoserver.game.core.model.ItemType;

import lombok.Data;

@Data
public class ItemPrototype {
    private ItemType type;
    private int modelId;
    private String name;
    private String displayName;
    private boolean movable;
    private int hue;
    private boolean hidden;
    private ContainerTypePrototype container;
    private MountTypePrototype mount;

    @Data
    public static class ContainerTypePrototype {
        private int gumpId;
    }

    @Data
    public static class MountTypePrototype {
        private String npc;
    }
}
