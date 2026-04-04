package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Setter
@Getter
public class UOCorpse extends UOContainer {

    private int corpseId;
    private final Map<Layer, Integer> equippedItems = new EnumMap<>(Layer.class);

    public UOCorpse(UOItemData data) {
        super(data);
        this.corpseId = data.getCorpseId();
        if (data.getEquippedItems() != null) {
            equippedItems.putAll(data.getEquippedItems());
        }
    }

    @Override
    protected void populateData(UOItemData data) {
        super.populateData(data);

        data.setCorpseId(corpseId);
        data.setEquippedItems(new EnumMap<>(equippedItems));
    }

    public void addEquippedItem(UOItem item) {

        if (item.getLayer() == null) {
            throw new IllegalArgumentException("Item must have layer");
        }

        equippedItems.put(item.getLayer(), item.getSerialId());

        super.addItemToContainer(item);

        item.setContainerSerialId(0);
    }

    public boolean removeEquippedItem(UOItem item) {

        var existing = equippedItems.get(item.getLayer());

        if (existing == item.getSerialId()) {

            equippedItems.remove(item.getLayer());

            super.removeItemFromContainer(item);

            item.setContainerSerialId(0);

            return true;
        }

        return false;
    }

}
