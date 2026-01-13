package com.github.mayconr.juoserver.game.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.mayconr.juoserver.game.core.prototype.ItemPrototype;

import lombok.Getter;

@Getter
public class UOContainer extends UOItem implements Container {

    private final Map<Integer, UOItem> itensInContainer = new HashMap<>();
    private int containerGumpId;

    public UOContainer(int serialId, ItemPrototype prototype, Location location) {
        super(serialId, prototype, location);
        this.containerGumpId = prototype.getContainer().getGumpId();
    }

    public UOContainer(UOItem item, int containerGumpId) {
        super(item);
        this.containerGumpId = containerGumpId;
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
