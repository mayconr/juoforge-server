package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface SessionInternal {

    void login(UOPlayer player);

    void logout(UOPlayer player);

}
