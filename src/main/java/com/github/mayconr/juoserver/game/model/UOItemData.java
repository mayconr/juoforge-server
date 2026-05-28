package com.github.mayconr.juoserver.game.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class UOItemData extends UOObjectData {
    private UUID id;
    private String template;
    private Layer layer;
    private int amount;
    private int hue;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private List<ItemFlag> flags;
    private int unitWeight;
    // Location
    private ItemLocationType locationType;
    private Integer ownerSerialId;
    private Integer containerSerialId;
    // Container
    private int containerGumpId;
    // Corpse
    private int corpseId;
    private Map<Layer, Integer> equippedItems = new EnumMap<>(Layer.class);
}
