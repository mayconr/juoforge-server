package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UONpc extends UOMobile {
    private final NpcType type;
    private int speechHue;
    private int speechFont;
    private String ai;
    private String mount;

    public UONpc(UOMobile mobile, NpcType type, String ai) {
        super(mobile);
        this.type = type;
        this.ai = ai;
    }

}
