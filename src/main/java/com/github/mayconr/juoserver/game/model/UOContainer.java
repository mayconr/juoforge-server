package com.github.mayconr.juoserver.game.model;

import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class UOContainer extends UOItem implements Container {

    private final Map<Integer, UOItem> itensInContainer = new ConcurrentHashMap<>();
    private int containerGumpId;

    /*public UOContainer(int serialId, ItemPrototype prototype, Location location) {
        super(serialId, prototype, location);
        this.containerGumpId = prototype.getContainer().getGumpId();
    }*/

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
