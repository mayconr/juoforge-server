package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.npc.NpcSession;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WorldSession {

    /*
        GENERAL USAGE METHODS
     */
    void initialize();

    void sendBroadcastMessage(String message);

    /*
        MOBILE METHODS
     */
    CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId);

    CompletableFuture<PlayerSession> createAndLoginPlayer(UOAccount account, CreateCharacter character, SessionOutbound outbound);

    CompletableFuture<PlayerSession> loginExistingPlayer(UOPlayer player, SessionOutbound outbound);

    PlayerSession getPlayerSession(UOMobile mobile);

    CompletableFuture<List<UOMobile>> getMobilesInRange(Location location);

    MovementResult tryMove(UOMobile mobile, MoveRequest request);

    MovementResult tryMove(UOMobile mobile, Location location);

    void applyMove(UOMobile mobile, MovementResult result);

    boolean isMobile(int serialId);

    void deleteMobile(int serialId);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<NpcSession> createNpcSession(String name, Location location);

    CompletableFuture<UONpc> createNpcAtLocation(String name, Location location);

    /*
        ITEM METHODS
     */
    CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId);

    CompletableFuture<UOItem> createItemAtLocation(String name, Location location);

    void deleteItem(int serial);

    void deleteItem(UOItem item);

    void moveItem(UOItem item, Location location);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    CompletableFuture<List<UOItem>> getItemsInRange(Location location);

    CompletableFuture<Optional<Container>> findContainerBySerialId(int serialId);

    boolean isItem(int serialId);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);


}
