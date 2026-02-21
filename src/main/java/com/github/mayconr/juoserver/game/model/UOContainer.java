package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UOContainer extends UOItem implements Container {

    private final Map<Integer, UOItem> itensInContainer = new ConcurrentHashMap<>();
    @Getter
    private int containerGumpId;

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
