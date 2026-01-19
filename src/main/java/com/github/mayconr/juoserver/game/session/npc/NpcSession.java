package com.github.mayconr.juoserver.game.session.npc;

import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;

public interface NpcSession {

    UONpc getNpc();

    void walk(Direction direction);

    void speech(String message);

    void move(Direction direction);

    void move(Location location);
}
