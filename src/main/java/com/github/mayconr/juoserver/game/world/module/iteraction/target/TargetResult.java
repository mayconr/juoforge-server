package com.github.mayconr.juoserver.game.world.module.iteraction.target;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.TargetType;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public record TargetResult(UOPlayer sender, TargetType type, int serialId, int modelId, Location location) {

    public boolean isObject() {
        return TargetType.OBJECT.equals(type);
    }

    public boolean isStatics() {
        return TargetType.STATICS.equals(type);
    }

}
