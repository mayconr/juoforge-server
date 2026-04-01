package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ToString(callSuper = true)
public class UOContainer extends UOItem implements Container {

    private final Map<Integer, UOItem> itemsInContainer = new ConcurrentHashMap<>();

    @Getter
    private final int containerGumpId;

    public UOContainer(UOItemData data) {
        super(data);
        this.containerGumpId = data.getContainerGumpId();
    }

    @Override
    public UOItemData toData() {
        UOItemData base = super.toData();

        return UOItemData.builder()
                .serialId(base.getSerialId())
                .modelId(base.getModelId())
                .x(base.getX())
                .y(base.getY())
                .z(base.getZ())
                .name(base.getName())
                .displayName(base.getDisplayName())
                .persistentAttrMap(base.getPersistentAttrMap())

                .id(base.getId())
                .layer(base.getLayer())
                .amount(base.getAmount())
                .hue(base.getHue())
                .unitWeight(base.getUnitWeight())
                .movable(base.isMovable())
                .hidden(base.isHidden())
                .direction(base.getDirection())
                .containerSerialId(base.getContainerSerialId())
                .ownerSerialId(base.getOwnerSerialId())
                .flags(base.getFlags())

                .containerGumpId(containerGumpId)
                .build();
    }

    public UOContainer(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap, UUID id, Layer layer, int amount, int hue, List<ItemFlag> flags, int unitWeight) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap, id, layer, amount, hue, flags, unitWeight);
        this.containerGumpId = 0;
    }

    @Override
    public void addItemsToContainer(List<UOItem> items) {
        for (UOItem item : items) {
            this.addItemToContainer(item);
        }
    }

    @Override
    public void addItemToContainer(UOItem item) {
        item.setOwnerSerialId(0);
        item.setContainerSerialId(this.getSerialId());
        itemsInContainer.put(item.getSerialId(), item);
    }

    @Override
    public Collection<UOItem> getItemsInContainer() {
        return itemsInContainer.values();
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        item.setContainerSerialId(0);
        itemsInContainer.remove(item.getSerialId());
    }

}
