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
    private Layer layer;
    private int amount;
    private int hue;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private Container container;

    /** Npc created after a player unmount. Only for mountType: MOUNT */
    private String mountNpc;

    public UOItem(
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            ItemType type,
            Layer layer,
            int amount,
            int hue,
            boolean movable,
            boolean hidden,
            Direction direction,
            Container container,
            String mountNpc) {
        super(serialId, modelId, x, y, z, name);
        this.type = type;
        this.layer = layer;
        this.amount = amount;
        this.hue = hue;
        this.movable = movable;
        this.hidden = hidden;
        this.direction = direction;
        this.container = container;
        this.mountNpc = mountNpc;
    }

    public UOItem(UOItem other) {
        super(
                other.getSerialId(),
                other.getModelId(),
                other.getX(),
                other.getY(),
                other.getZ(),
                other.getName()
        );

        this.type = other.type;
        this.layer = other.layer;
        this.amount = other.amount;
        this.hue = other.hue;
        this.movable = other.movable;
        this.hidden = other.hidden;
        this.direction = other.direction;
        this.container = other.container;   // cópia por referência (ver observação abaixo)
        this.mountNpc = other.mountNpc;
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
