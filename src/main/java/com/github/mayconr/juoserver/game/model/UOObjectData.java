package com.github.mayconr.juoserver.game.model;

import lombok.Data;

@Data
public class UOObjectData {

    private int serialId;

    private int modelId;

    private int x;

    private int y;

    private int z;

    private String name;

    private String displayName;

    private AttributeMap persistentAttrMap;
}
