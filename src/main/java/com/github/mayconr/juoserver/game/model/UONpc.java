package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class UONpc extends UOMobile {
    private final NpcType type;
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;
    private final List<NpcRole> roles = new ArrayList<>();

    public UONpc(UOMobile mobile, NpcType type) {
        super(mobile);
        this.type = type;
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
