package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.network.packet.MoveRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface WorldService {

    CompletableFuture<Optional<UOMobile>> findMobileBySerialId(int serialId);

    CompletableFuture<Optional<UOItem>> findItemBySerialId(int serialId);

    CompletableFuture<Optional<Container>> findContainerBySerialId(int serialId);

    List<UOCity> getCities();

    CompletableFuture<List<UOMobile>> getMobilesInRange(Location location);

    MovementResult tryMove(UOMobile mobile, MoveRequest request);

    void applyMove(UOMobile mobile, MovementResult result);

    void deleteMobile(UOMobile mobile);

    CompletableFuture<UOPlayer> createPlayer(PlayerDetails details);

    UONpc createNpcAtLocation(String name, Location location);

    /**
     * Item will be created at informed location and added to the ground items (will be updated on
     * movements)
     *
     * @param name Item name
     * @param location Location
     * @return Created item
     */
    UOItem createItemOnTheGround(String name, Location location);

    /**
     * Create a new item without a location
     *
     * @param name Item name
     * @return created item
     */
    UOItem createItem(String name);

    /**
     * Item will be created at informed location, but will not be added to the ground items
     *
     * @param name Item Name
     * @param location Location
     * @return Created item
     */
    UOItem createItem(String name, Location location);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    CompletableFuture<List<UOItem>> getItemsInRange(Location location);

    void deleteItem(UOItem item);

    boolean isMobile(int serialId);

    CompletableFuture<Collection<UOMobile>> saveMobileRuntime();

    CompletableFuture<Collection<UOMobile>> saveMobileVitals();

    CompletableFuture<Collection<UOMobile>> saveMobileAttributes();

    CompletableFuture<Collection<UOMobile>> saveMobiles();
}
