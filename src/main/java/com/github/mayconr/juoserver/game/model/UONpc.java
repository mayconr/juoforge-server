package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class UONpc extends UOMobile {
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;
    private final List<String> roles = new ArrayList<>();

    public UONpc(Integer serialId, Integer modelId, Integer x, Integer y, Integer z, String name, String displayName, AttributeMap persistentAttrMap) {
        super(serialId, modelId, x, y, z, name, displayName, persistentAttrMap);
    }

    public UONpc(UOMobile mobile) {
        super(mobile);
    }

    public void addRole(String role) {
        roles.add(role);
    }

}
