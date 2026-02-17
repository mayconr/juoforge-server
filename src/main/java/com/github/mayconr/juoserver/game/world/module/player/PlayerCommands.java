package com.github.mayconr.juoserver.game.world.module.player;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PlayerCommands {
    CompletableFuture<UOPlayer> createNewPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account);

    void spawn(UOPlayer player);

    void despawn(UOPlayer player);

    void deletePlayer(UOPlayer player);
}
