package com.github.mayconr.juoserver.game.model;

import java.util.Optional;

import com.github.mayconr.juoserver.game.core.prototype.NpcPrototype;

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

    public UONpc(UOMobile mobile) {
        super(mobile);
        this.type = NpcType.MONSTER;
    }

    public UONpc(int serialId, NpcPrototype prototype, Location location) {
        super(
                serialId,
                prototype.getModelId(),
                location.getX(),
                location.getY(),
                location.getZ(),
                prototype.getDisplayName(),
                Direction.NORTH,
                prototype.getHue(),
                CharacterStatus.NORMAL,
                prototype.getNotoriety(),
                prototype.getRace(),
                prototype.getGender());
        this.type = prototype.getType();
        this.speechHue = prototype.getSpeechHue();
        this.speechFont = prototype.getSpeechFont();
        this.ai = prototype.getAi();
        this.mount =
                Optional.ofNullable(prototype.getMount())
                        .map(NpcPrototype.MountTypePrototype::getName)
                        .orElse(null);
    }
}
