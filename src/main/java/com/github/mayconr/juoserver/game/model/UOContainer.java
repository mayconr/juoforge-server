package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    protected void populateData(UOItemData data) {
        super.populateData(data);

        data.setContainerGumpId(containerGumpId);
    }

    @Override
    public void addItemsToContainer(List<UOItem> items) {
        for (UOItem item : items) {
            this.addItemToContainer(item);
        }
    }

    @Override
    public void addItemToContainer(UOItem item) {
        item.addToContainer(this);
        itemsInContainer.put(item.getSerialId(), item);
    }

    @Override
    public void addItemToContainer(UOItem item, Location locationInContainer) {
        item.addToContainer(this, locationInContainer);
        itemsInContainer.put(item.getSerialId(), item);
    }

    @Override
    public Collection<UOItem> getContainerItems() {
        return itemsInContainer.values();
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        item.removeFromContainer();
        itemsInContainer.remove(item.getSerialId());
    }

}
