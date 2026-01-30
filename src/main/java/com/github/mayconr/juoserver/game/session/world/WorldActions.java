package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;

public interface WorldActions {

    void sendBroadcastMessage(String message);

    UONpc createNpc(String name, Location location);

    UOItem createItem(String name, Location location);

}
