package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;

public interface WorldNpc {

    /** @param requester player or asynchronous process requesting creation */
    UONpc createNpc(NpcRequester requester, String template, Location location);

    void removeNpc(NpcRequester requester, UONpc npc);
}
