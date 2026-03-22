package com.github.mayconr.juoserver.game.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UOObject implements Location, AttributeSupport, TooltipSupport {
    @Getter
    @EqualsAndHashCode.Include @ToString.Include private int serialId;
    @Getter
    @Setter
    private int modelId;
    @Getter
    @Setter
    private int x;
    @Getter
    @Setter
    private int y;
    @Getter
    @Setter
    private int z;
    @Getter
    @Setter
    @ToString.Include private String name;
    @Getter
    @Setter
    private String displayName;

    private final AttributeMap persistentAttrMap;
    private final AttributeMap runtimeAttrMap;

    public UOObject(int serialId, int modelId, int x, int y, int z, String name, String displayName, AttributeMap persistentAttrMap) {
        this.serialId = serialId;
        this.modelId = modelId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.displayName = displayName;
        this.persistentAttrMap = persistentAttrMap;
        this.runtimeAttrMap = new DefaultAttributeMap();
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setLocation(Location location) {
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
    }

    @Override
    public AttributeMap persistentAttributes() {
        return persistentAttrMap;
    }

    @Override
    public AttributeMap runtimeAttributes() {
        return runtimeAttrMap;
    }

    @Override
    public String getTooltipText() {
        return displayName;
    }

    @Override
    public int getTooltipId() {
        return serialId;
    }
}
