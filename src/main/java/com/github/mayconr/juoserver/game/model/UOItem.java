package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UOItem extends UOObject<UOItemData> {

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
    protected UOItemData createData() {
        return new UOItemData();
    }

    @Override
    protected void populateData(UOItemData data) {
        super.populateData(data);

        data.setId(id);
        data.setLayer(layer);
        data.setAmount(amount);
        data.setHue(hue);
        data.setMovable(movable);
        data.setHidden(hidden);
        data.setDirection(direction);
        data.setContainerSerialId(containerSerialId);
        data.setFlags(flags);
        data.setUnitWeight(unitWeight);
        data.setOwnerSerialId(ownerSerialId);
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

    public void dropOnTheGround(Location location) {
        ownerSerialId = null;
        containerSerialId = null;
        setLocation(location);
    }

    public void equip(UOMobile mobile) {
        containerSerialId = null;
        setLocation(0,0,0);
        ownerSerialId = mobile.getSerialId();
    }

    public void unequip() {
        containerSerialId = null;
    }

    public void addToContainer(UOContainer container) {
        ownerSerialId = null;
        containerSerialId = container.getSerialId();
    }

    public void addToContainer(UOContainer container, Location locationInContainer) {
        ownerSerialId = null;
        setLocation(locationInContainer);
        containerSerialId = container.getSerialId();
    }

    public void removeFromContainer() {
        containerSerialId = null;
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
