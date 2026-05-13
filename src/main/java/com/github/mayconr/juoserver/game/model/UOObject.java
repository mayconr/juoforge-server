package com.github.mayconr.juoserver.game.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class UOObject<T extends UOObjectData> implements Location, AttributeSupport, TooltipSupport {
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

    protected UOObject(UOObjectData data) {
        this.serialId = data.getSerialId();
        this.modelId = data.getModelId();
        this.x = data.getX();
        this.y = data.getY();
        this.z = data.getZ();
        this.name = data.getName();
        this.displayName = data.getDisplayName();
        this.persistentAttrMap = data.getPersistentAttrMap();
        this.runtimeAttrMap = new DefaultAttributeMap();
    }

    public T toData() {
        T data = createData();

        populateData(data);

        return data;
    }

    protected abstract T createData();

    protected void populateData(T data) {
        data.setSerialId(serialId);
        data.setModelId(modelId);
        data.setX(x);
        data.setY(y);
        data.setZ(z);
        data.setName(name);
        data.setDisplayName(displayName);
        data.setPersistentAttrMap(persistentAttrMap);
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
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
