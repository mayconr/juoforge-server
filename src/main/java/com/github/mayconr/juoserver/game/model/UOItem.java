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
    private ItemLocation currentLocation;
    private ItemLocation previousLocation;
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
        this.flags = data.getFlags();
        this.unitWeight = data.getUnitWeight();
        this.currentLocation = switch (data.getLocationType() == null ? ItemLocationType.EQUIPPED : data.getLocationType()) {
            case EQUIPPED -> ItemLocation.equipped(data.getOwnerSerialId());
            case CONTAINER -> ItemLocation.container(data.getContainerSerialId());
            case GROUND ->  ItemLocation.ground();
            case ORPHAN -> ItemLocation.orphan();
        };
    }

    public static boolean isItem(int serialId) {
        return serialId >= OBJECTS_MIN_SERIAL_ID;
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

        switch (currentLocation) {
            case EquippedLocation location -> {
                data.setOwnerSerialId(location.ownerSerialId());
                data.setLocationType(ItemLocationType.EQUIPPED);
            }
            case ContainerLocation location -> {
                data.setContainerSerialId(location.containerSerialId());
                data.setLocationType(ItemLocationType.CONTAINER);
            }
            case GroundLocation location -> data.setLocationType(ItemLocationType.GROUND);
            case OrphanLocation location -> {
                data.setLocationType(ItemLocationType.ORPHAN);
            }
        }
        data.setFlags(flags);
        data.setUnitWeight(unitWeight);
    }

    public boolean hasFlag(ItemFlag flag) {
        return flags.contains(flag);
    }

    public void increaseAmount(int amount) {
        this.amount += amount;
    }

    public void setCurrentLocation(ItemLocation currentLocation) {
        this.previousLocation = this.currentLocation;
        this.currentLocation = currentLocation;
    }

    /*
        Item lifecycle methods
     */
    public boolean isEquipped() {
        return currentLocation instanceof EquippedLocation;
    }

    public boolean isOnTheGround() {
        return currentLocation instanceof GroundLocation;
    }

    public boolean isInContainer() {
        return currentLocation instanceof ContainerLocation;
    }

    public boolean isOrphan() {
        return currentLocation instanceof OrphanLocation;
    }
}
