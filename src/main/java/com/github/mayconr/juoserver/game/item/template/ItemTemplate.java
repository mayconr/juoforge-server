package com.github.mayconr.juoserver.game.item.template;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.template.BaseTemplate;

import java.util.*;

public record ItemTemplate(String name,
                           String displayName,
                           Layer layer,
                           int modelId,
                           boolean movable,
                           int hue,
                           String stockType,
                           int basePrice,
                           List<ItemFlag> flags,
                           Map<String, Object> attr,

                           // Cotainer
                           int containerGumpId,
                           // Mount
                           String mountName)

            implements BaseTemplate {
    public ItemTemplate {
        attr = attr == null ? Map.of() : Map.copyOf(attr);
        flags = flags == null ? Collections.emptyList() : flags;
    }

    public UOItemData createData(int serialId) {
        final var data = new UOItemData();
        data.setId(UUID.randomUUID());
        data.setSerialId(serialId);
        data.setModelId(modelId);
        data.setX(0);
        data.setY(0);
        data.setZ(0);
        data.setName(name);
        data.setDisplayName(displayName);
        data.setPersistentAttrMap(new DefaultAttributeMap(attr));
        data.setLayer(layer);
        data.setAmount(1);
        data.setHue(hue);
        data.setMovable(movable);
        data.setHidden(false);
        data.setDirection(Direction.NORTH);
        data.setOwnerSerialId(0);
        data.setFlags(flags);
        data.setContainerGumpId(containerGumpId);
        data.setMountName(mountName);
        return data;
    }
}
