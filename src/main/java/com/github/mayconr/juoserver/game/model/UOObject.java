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
    private final Map<String, Object> attrMap = new HashMap<>();

    public UOObject(int serialId, int modelId, int x, int y, int z, String name, String displayName, Map<String, Object> attrMap) {
        this.serialId = serialId;
        this.modelId = modelId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.displayName = displayName;
        this.attrMap.putAll(attrMap);
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
    public void addAttribute(String key, Object value) {
        attrMap.put(key, value);
    }

    @Override
    public <T> T getAttribute(String key, T defaultValue) {
        return (T) attrMap.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean hasAttribute(String key) {
        return attrMap.containsKey(key);
    }

    @Override
    public <T> T getAndSetAttribute(String key, T newValue) {
        T value = (T) attrMap.get(key);

        if (newValue != null) {
            attrMap.put(key, newValue);
        } else {
            attrMap.remove(key);
        }
        return value;
    }

}
