package com.github.mayconr.juoserver.game.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UOObject implements Location, AttributeSupport {
    @EqualsAndHashCode.Include @ToString.Include private int serialId;
    private int modelId;
    private int x;
    private int y;
    private int z;
    @ToString.Include private String name;
    private String displayName;
    private final Map<String, Object> persistentAttrMap = new HashMap<>();
    private final Map<String, Object> runtimeAttrMap = new HashMap<>();

    public UOObject(int serialId, int modelId, int x, int y, int z, String name, String displayName, Map<String, Object> persistentAttrMap) {
        this.serialId = serialId;
        this.modelId = modelId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.displayName = displayName;
        this.persistentAttrMap.putAll(persistentAttrMap);
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
    public void setPersistentAttribute(String key, Object value) {
        persistentAttrMap.put(key, value);
    }

    @Override
    public <T> T getPersistentAttribute(String key, T defaultValue) {
        return (T) persistentAttrMap.getOrDefault(key, defaultValue);
    }

    @Override
    public Object getPersistentAttribute(String key) {
        return persistentAttrMap.get(key);
    }

    @Override
    public void setRuntimeAttribute(String key, Object value) {
        runtimeAttrMap.put(key, value);
    }

    @Override
    public <T> T getRuntimeAttribute(String key, T defaultValue) {
        return (T) runtimeAttrMap.getOrDefault(key, defaultValue);
    }

    @Override
    public Object getRuntimeAttribute(String key) {
        return runtimeAttrMap.get(key);
    }
}
