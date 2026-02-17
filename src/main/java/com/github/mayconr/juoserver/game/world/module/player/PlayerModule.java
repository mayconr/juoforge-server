package com.github.mayconr.juoserver.game.world.module.player;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class PlayerModule implements WorldModule, PlayerCommands {

    private final PlayerCreationHandler playerCreationHandler;
    private final PlayerLoginHandler playerLoginHandler;
    private final PlayerRemovalHandler playerRemovalHandler;

    @Override
    public void update(double delta) {

    }

    @Override
    public CompletableFuture<UOPlayer> createNewPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account) {
        return playerCreationHandler.createNewPlayer(character, cities, account);
    }

    @Override
    public void spawn(UOPlayer player) {
        playerLoginHandler.spawn(player);
    }

    @Override
    public void despawn(UOPlayer player) {
        playerLoginHandler.despawn(player);
    }

    @Override
    public void deletePlayer(UOPlayer player) {
        playerRemovalHandler.deletePlayer(player);
    }
}
