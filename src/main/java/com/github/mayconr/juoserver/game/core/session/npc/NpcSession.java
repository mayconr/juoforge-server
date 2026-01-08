package com.github.mayconr.juoserver.game.core.session.npc;

import com.github.mayconr.juoserver.game.core.model.Direction;
import com.github.mayconr.juoserver.game.core.model.Location;
import com.github.mayconr.juoserver.game.core.model.UONpc;

public interface NpcSession {

    UONpc getNpc();

    void walk(Direction direction);

    void speech(String message);

    void move(Direction direction);

    void move(Location location);
}
