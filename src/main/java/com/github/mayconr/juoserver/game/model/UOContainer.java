package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ToString(callSuper = true)
public class UOContainer extends UOItem implements Container {

    private final Set<Integer> itemsInContainer = ConcurrentHashMap.newKeySet();

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
        itemsInContainer.add(item.getSerialId());
    }

    @Override
    public Collection<Integer> getContainerItems() {
        return List.copyOf(itemsInContainer);
    }

    @Override
    public void removeItemFromContainer(UOItem item) {
        itemsInContainer.remove(item.getSerialId());
    }

}
