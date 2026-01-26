package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class UOItem extends UOObject {

    private UUID id;
    private ItemType type;
    private Layer layer;
    private int amount;
    private int hue;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private Container container;
    private UOMobile owner;

    /** Npc created after a player unmount. Only for mountType: MOUNT */
    private String mountNpc;

    public UOItem(
            UUID id,
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            String displayName,
            Map<String, Object> attr,
            ItemType type,
            Layer layer,
            int amount,
            int hue,
            boolean movable,
            boolean hidden,
            Direction direction,
            Container container,
            String mountNpc) {
        super(serialId, modelId, x, y, z, name, displayName, attr);
        this.id = id;
        this.type = type;
        this.layer = layer;
        this.amount = Math.max(1, amount);
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
                other.getName(),
                other.getDisplayName(),
                other.getAttrMap()
        );
        this.id = other.id;
        this.type = other.type;
        this.layer = other.layer;
        this.amount = other.amount;
        this.hue = other.hue;
        this.movable = other.movable;
        this.hidden = other.hidden;
        this.direction = other.direction;
        this.container = other.container;
        this.owner = other.getOwner();
        this.mountNpc = other.mountNpc;
    }

}
