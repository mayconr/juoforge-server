package com.github.mayconr.juoserver.game.core.model;

import java.util.Optional;

import com.github.mayconr.juoserver.game.core.prototype.ItemPrototype;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UOItem extends UOObject {

    private ItemType type;
    private int amount;
    private int hue;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private Container container;

    /** Npc created after a player unmount. Only for mountType: MOUNT */
    private String mountNpc;

    public UOItem(
            int serialId, int modelId, int x, int y, int z, String name, int amount, int hue) {
        super(serialId, modelId, x, y, z, name);
        this.amount = amount;
        this.hue = hue;
    }

    public UOItem(int serialId, ItemPrototype prototype, Location location) {
        super(
                serialId,
                prototype.getModelId(),
                location.getX(),
                location.getY(),
                location.getZ(),
                prototype.getDisplayName());
        this.type = prototype.getType();
        this.movable = prototype.isMovable();
        this.hue = prototype.getHue();
        this.hidden = prototype.isHidden();
        this.mountNpc =
                Optional.ofNullable(prototype.getMount())
                        .map(ItemPrototype.MountTypePrototype::getNpc)
                        .orElse(null);
    }
}
