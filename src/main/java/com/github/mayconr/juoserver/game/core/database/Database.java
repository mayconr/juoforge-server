package com.github.mayconr.juoserver.game.core.database;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.mayconr.juoserver.game.core.model.*;

public interface Database {

    Optional<UOAccount> getAccount(String username, String password);

    Optional<UOAccount> getAccount(String accountId);

    List<UOPlayer> getPlayersByAccount(UOAccount UOAccount);

    Optional<UOMobile> getMobileSerialId(int serialId);

    Optional<UOItem> getItemBySerialId(int serialId);

    Optional<Container> getContainerById(int serialId);

    List<UOCity> getCities();

    Stream<UOMobile> getMobilesInRange(Location location, MobileFilter filter);

    void deleteMobile(UOMobile mobile);

    UOPlayer createPlayer(PlayerDetails details);

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

    List<UOItem> getItemsInRange(Location location);

    void deleteItem(UOItem item);

    boolean isMobile(int serialId);
}
