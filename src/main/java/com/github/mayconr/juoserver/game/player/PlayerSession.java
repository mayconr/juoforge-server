package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public interface PlayerSession {

    SessionOutbound getOutbound();

    UOPlayer getPlayer();

    void deactivate();

    void initialize(WorldInternal worldInternal, String clientVersion);

}
