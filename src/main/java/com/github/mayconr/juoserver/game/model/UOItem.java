package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class UOItem extends UOObject {

    public static final int OBJECTS_MIN_SERIAL_ID = 0x3FFFFFFF + 1;
    private UUID id;
    private Layer layer;
    private int amount;
    private int hue;
    private int unitWeight;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private Container container;
    private UOMobile owner;
    private List<ItemFlag> flags;
    private int corpseId;

    public UOItem(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap, UUID id, Layer layer, int amount, int hue, List<ItemFlag> flags, int unitWeight, int corpseId) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
        this.id = id;
        this.layer = layer;
        this.amount = amount;
        this.hue = hue;
        this.flags = flags;
        this.unitWeight = unitWeight;
        this.corpseId = corpseId;
        /*this.movable = movable;
        this.hidden = hidden;
        this.direction = direction;
        this.owner = owner;
        this.flags = flags;*/
    }

    public UOItem(
            UUID id,
            int serialId,
            int modelId,
            int x,
            int y,
            int z,
            String name,
            String displayName,
            AttributeMap attr,
            Layer layer,
            int amount,
            int hue,
            boolean movable,
            boolean hidden,
            Direction direction,
            Container container,
            List<ItemFlag> flags) {
        super(serialId, modelId, x, y, z, name, displayName, attr);
        this.id = id;
        this.layer = layer;
        this.amount = Math.max(1, amount);
        this.hue = hue;
        this.movable = movable;
        this.hidden = hidden;
        this.direction = direction;
        this.container = container;
        this.flags = flags;
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
                other.persistentAttributes()
        );
        this.id = other.id;
        this.layer = other.layer;
        this.amount = other.amount;
        this.hue = other.hue;
        this.movable = other.movable;
        this.hidden = other.hidden;
        this.direction = other.direction;
        this.container = other.container;
        this.owner = other.owner;
        this.flags = other.flags;
        this.unitWeight = other.unitWeight;
        this.corpseId = other.corpseId;
    }

    public static boolean isItem(int serialId) {
        return serialId >= OBJECTS_MIN_SERIAL_ID;
    }

    public boolean isOnTheGround() {
        return owner == null && container == null;
    }

    public boolean isInContainer() {
        return container != null && owner == null;
    }

    public boolean isEquipped() {
        return owner != null && container == null;
    }

    public boolean hasFlag(ItemFlag flag) {
        return flags.contains(flag);
    }

    public void increaseAmount(int amount) {
        this.amount += amount;
    }

    public void removeWhenInContainer() {
        if (isInContainer()) {
            container.removeItemFromContainer(this);
            container = null;
        }
    }
}
