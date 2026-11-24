package com.github.mayconr.juoserver.infrastructure.region;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.RegionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ToString(onlyExplicitlyIncluded = true)
@Getter
@RequiredArgsConstructor
public class RegionNode {
    @ToString.Include
    private final String name;
    @ToString.Include
    private final String displayName;
    @ToString.Include
    private final RegionType type;
    @ToString.Include
    private final RegionArea area;

    @Setter
    private boolean aiActivationScope;

    @Setter
    private RegionLore lore;

    @Setter
    private RegionTeleport teleport;

    @Setter
    private RegionNode parent;
    private final List<RegionNode> children = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();

    public void addChild(RegionNode child) {
        Objects.requireNonNull(child);
        child.setParent(this);
        this.children.add(child);
    }

    public Optional<RegionNode> getParent() {
        return Optional.ofNullable(parent);
    }

    public Optional<RegionLore> getLore() {
        return Optional.ofNullable(lore);
    }

    public List<RegionNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    // ---------------------------
    // Area
    // ---------------------------

    public boolean contains(Location location) {
        return area.contains(location);
    }

    // ---------------------------
    // Properties
    // ---------------------------

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolveProperty(String key) {
        Objects.requireNonNull(key);

        if (properties.containsKey(key)) {
            return (T) properties.get(key);
        }

        if (parent != null) {
            return parent.resolveProperty(key);
        }

        return null;
    }
}
