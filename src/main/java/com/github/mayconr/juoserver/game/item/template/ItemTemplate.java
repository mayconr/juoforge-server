package com.github.mayconr.juoserver.game.item.template;

import com.github.mayconr.juoserver.game.model.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
                           String mountName,
                           // Weapon
                           Weapon weapon)

            implements BaseTemplate {
    public ItemTemplate {
        attr = attr == null ? Map.of() : Map.copyOf(attr);
        flags = flags == null ? Collections.emptyList() : flags;
    }

    /** Nullable JSON input distinguishes an omitted movable flag from explicit false. */
    @JsonCreator
    public ItemTemplate(
            @JsonProperty("name") String name,
            @JsonProperty("displayName") String displayName,
            @JsonProperty("layer") Layer layer,
            @JsonProperty("modelId") int modelId,
            @JsonProperty("movable") Boolean movable,
            @JsonProperty("hue") int hue,
            @JsonProperty("stockType") String stockType,
            @JsonProperty("basePrice") int basePrice,
            @JsonProperty("flags") List<ItemFlag> flags,
            @JsonProperty("attr") Map<String, Object> attr,
            @JsonProperty("containerGumpId") int containerGumpId,
            @JsonProperty("mountName") String mountName,
            @JsonProperty("weapon") Weapon weapon) {
        this(name, displayName, layer, modelId, movable == null || movable.booleanValue(), hue,
                stockType, basePrice, flags, attr, containerGumpId, mountName, weapon);
    }

    public record Weapon(WeaponStyle style, BaseDamage baseDamage, int radius) { }

    public record BaseDamage(int min, int max) {}

    public UOItemData toData(int serialId) {
        final var data = new UOItemData();
        data.setId(UUID.randomUUID());
        data.setTemplate(name);
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
        data.setFlags(flags);
        data.setContainerGumpId(containerGumpId);
        data.setLocationType(ItemLocationType.ORPHAN);
        return data;
    }
}
