package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
    private Integer containerSerialId;
    private Integer ownerSerialId;
    private List<ItemFlag> flags;

    public UOItem(UOItemData data) {
        super(data);
        this.id = data.getId();
        this.layer = data.getLayer();
        this.amount = data.getAmount();
        this.hue = data.getHue();
        this.movable = data.isMovable();
        this.hidden = data.isHidden();
        this.direction = data.getDirection();
        this.containerSerialId = data.getContainerSerialId();
        this.flags = data.getFlags();
        this.unitWeight = data.getUnitWeight();
    }

    @Override
    public UOItemData toData() {
        UOObjectData base = super.toData();

        return UOItemData.builder()
                .serialId(base.getSerialId())
                .modelId(base.getModelId())
                .x(base.getX())
                .y(base.getY())
                .z(base.getZ())
                .name(base.getName())
                .displayName(base.getDisplayName())
                .persistentAttrMap(base.getPersistentAttrMap())

                .id(id)
                .layer(layer)
                .amount(amount)
                .hue(hue)
                .movable(movable)
                .hidden(hidden)
                .direction(direction)
                .containerSerialId(containerSerialId)
                .flags(flags)
                .unitWeight(unitWeight)
                .ownerSerialId(ownerSerialId)
                .build();
    }

    public UOItem(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap, UUID id, Layer layer, int amount, int hue, List<ItemFlag> flags, int unitWeight) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
        this.id = id;
        this.layer = layer;
        this.amount = amount;
        this.hue = hue;
        this.flags = flags;
        this.unitWeight = unitWeight;
        /*this.movable = movable;
        this.hidden = hidden;
        this.direction = direction;
        this.owner = owner;
        this.flags = flags;*/
    }

    public static boolean isItem(int serialId) {
        return serialId >= OBJECTS_MIN_SERIAL_ID;
    }

    public boolean isOnTheGround() {
        return ownerSerialId == null && containerSerialId == null;
    }

    public boolean isInContainer() {
        return containerSerialId != null && ownerSerialId == null;
    }

    public boolean isEquipped() {
        return ownerSerialId != null && containerSerialId == null;
    }

    public boolean hasFlag(ItemFlag flag) {
        return flags.contains(flag);
    }

    public void increaseAmount(int amount) {
        this.amount += amount;
    }

    public void removeWhenInContainer() {
        if (isInContainer()) {
            containerSerialId = null;
        }
    }
}
