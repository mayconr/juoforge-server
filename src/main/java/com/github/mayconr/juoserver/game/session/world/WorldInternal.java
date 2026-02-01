package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView {

    void initialize();

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOMobile> unloadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<UOItem> unloadItem(int serialId);

    CompletableFuture<PlayerSession> createAndLoginPlayer(CreateCharacter character, SessionOutbound outbound);

    CompletableFuture<PlayerSession> loginExistingPlayer(UOPlayer player, SessionOutbound outbound);

    PlayerSession getPlayerSession(UOMobile mobile);

    CompletableFuture<List<UOMobile>> getMobilesInRange(Location location);

    MovementResult tryMove(UOMobile mobile, MoveRequest request);

    MovementResult tryMove(UOMobile mobile, Location location);

    void applyMove(UOMobile mobile, MovementResult result);

    boolean isMobile(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    CompletableFuture<List<UOItem>> getItemsInRange(Location location);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    void sendSkill(UOMobile mobile, SkillValue value);
}
