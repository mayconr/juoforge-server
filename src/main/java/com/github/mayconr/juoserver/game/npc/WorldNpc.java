package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;

public interface WorldNpc {

    /**
     * Creates a new NPC from a content template at the given location.
     *
     * @param requester origin of the request: a player or a named internal process;
     *                  this identifies the caller and does not change execution scheduling
     * @param template NPC template name, such as {@code "horse"}, defining its appearance, stats and AI
     * @param location spawn position; a mobile may be supplied to use its current position
     * @return the created NPC, or {@code null} if the flow did not create one
     */
    UONpc createNpc(NpcRequester requester, String template, Location location);

    void removeNpc(NpcRequester requester, UONpc npc);
}
