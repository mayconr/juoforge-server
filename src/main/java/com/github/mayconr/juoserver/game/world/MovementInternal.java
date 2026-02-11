package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

public interface MovementInternal {
    void move(UOPlayer player, MoveRequest moveRequest);

    void move(UOPlayer player, Location location);
}
