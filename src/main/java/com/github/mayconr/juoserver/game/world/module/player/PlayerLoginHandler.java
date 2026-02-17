package com.github.mayconr.juoserver.game.world.module.player;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedOut;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class PlayerLoginHandler {

    @Getter
    private final Map<Integer, UOPlayer> onlinePlayers = new ConcurrentHashMap<>();
    private final EventBus eventBus;
    private final RealmStorage storage;

    public void spawn(UOPlayer player) {
        onlinePlayers.put(player.getSerialId(), player);
        eventBus.publish(new PlayerLoggedIn(player));
    }

    public void despawn(UOPlayer player) {
        onlinePlayers.remove(player.getSerialId());
        storage.unloadMobile(player);
        eventBus.publish(new PlayerLoggedOut(player));
    }

}
