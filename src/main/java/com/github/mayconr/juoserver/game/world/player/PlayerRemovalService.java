package com.github.mayconr.juoserver.game.world.player;

import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.PlayerDeleted;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerRemovalService {

    private final RealmStorage storage;
    private final EventBus eventBus;

    public void deletePlayer(UOPlayer player) {
        storage.deleteMobile(player);
        eventBus.publish(new PlayerDeleted(player));
    }

}
