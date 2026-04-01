package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

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
                .containerGumpId(base.getContainerGumpId())
                .ownerSerialId(base.getOwnerSerialId())
                .flags(base.getFlags())

                .corpseId(corpseId)
                .equippedItems(new EnumMap<>(equippedItems))
                .build();
    }

    public UOCorpse(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap, UUID id, Layer layer, int amount, int hue, List<ItemFlag> flags, int unitWeight, int corpseId, int corpseOwnerId) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap, id, layer, amount, hue, flags, unitWeight);
        this.corpseId = corpseId;
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
