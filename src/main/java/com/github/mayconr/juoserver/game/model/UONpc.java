package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UONpc extends UOMobile {
    private final NpcType type;
    private int speechHue;
    private int speechFont;
    private BehaviorDefinition behavior;

    public UONpc(UOMobile mobile, NpcType type) {
        super(mobile);
        this.type = type;
    }

}
