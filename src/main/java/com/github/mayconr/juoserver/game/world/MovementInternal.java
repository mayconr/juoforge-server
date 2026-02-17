package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

public interface MovementInternal {
    void move(UOMobile mobile, MoveRequest moveRequest);
}
