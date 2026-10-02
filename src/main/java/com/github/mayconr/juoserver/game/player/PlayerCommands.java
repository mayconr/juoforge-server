package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PlayerCommands extends WorldPlayer {
    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOPlayer> createPlayerMobile(CreateCharacter character, Map<Integer, RegionNode> cities, UOAccount account);

    void spawn(UOPlayer player);

    void despawn(UOPlayer player);

}
