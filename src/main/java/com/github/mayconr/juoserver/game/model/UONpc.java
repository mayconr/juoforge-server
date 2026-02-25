package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class UONpc extends UOMobile {
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;
    private final List<NpcRole> roles = new ArrayList<>();

    public UONpc(int serialId, int modelId, int x, int y, int z, String name, String displayName, Map<String, Object> persistentAttrMap) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
    }

    public UONpc(UOMobile mobile) {
        super(mobile);
    }

    public void addRole(NpcRole role) {
        roles.add(role);
    }

    public <T extends NpcRole> Optional<T> getRole(Class<T> type) {
        return roles.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findFirst();
    }
}
