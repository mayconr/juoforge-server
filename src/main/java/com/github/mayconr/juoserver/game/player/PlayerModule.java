package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Internal player module with lifecycle, character creation and session operations. */
public interface PlayerModule extends WorldPlayer, WorldModule {
    CompletableFuture<UOPlayer> createPlayerMobile(CreateCharacter character, Map<Integer, RegionNode> startingLocations, UOAccount account);

    CompletableFuture<UOMobile> loadMobile(int serialId);

    void spawn(UOPlayer player);

    void despawn(UOPlayer player);
}
