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

    private final Map<Integer, UOItem> itensInContainer = new ConcurrentHashMap<>();

    @Getter
    private final int containerGumpId;

    public UOContainer(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap, UUID id, Layer layer, int amount, int hue, List<ItemFlag> flags, int unitWeight) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap, id, layer, amount, hue, flags, unitWeight);
        this.containerGumpId = persistentAttrMap.getOrDefault("gumpId", 0);
    }

    public UOContainer(UOItem item, int containerGumpId) {
        super(item);
        this.containerGumpId = containerGumpId;
    }

    @Override
    public void addItemsToContainer(List<UOItem> items) {
        for (UOItem item : items) {
            this.addItemToContainer(item);
        }
    }

    @Override
    public void addItemToContainer(UOItem item) {
        item.setOwner(null);
        item.setContainer(this);
        itensInContainer.put(item.getSerialId(), item);
    }

    @Override
    public Collection<UOItem> getItemsInContainer() {
        return itensInContainer.values();
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        item.setContainer(null);
        itensInContainer.remove(item.getSerialId());
    }

}
