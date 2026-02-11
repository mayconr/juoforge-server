package com.github.mayconr.juoserver.game.world.player;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedOut;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerLoginService {

    private final EventBus  eventBus;

    public void login(UOPlayer  player) {
        eventBus.publish(new PlayerLoggedIn(player));
    }

    public void logout(UOPlayer player) {
        eventBus.publish(new PlayerLoggedOut(player));
    }
}
