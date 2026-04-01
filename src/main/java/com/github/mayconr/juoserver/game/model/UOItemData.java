package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
public class UOItemData extends UOObjectData{
    private UUID id;
    private Layer layer;
    private int amount;
    private int hue;
    private boolean movable;
    private boolean hidden;
    private Direction direction;
    private Integer containerSerialId;
    private List<ItemFlag> flags;
    private int unitWeight;
    private Integer ownerSerialId;

    // Container
    private int containerGumpId;

    // Corpse
    private int corpseId;
    private Map<Layer, Integer> equippedItems = new EnumMap<>(Layer.class);
}
