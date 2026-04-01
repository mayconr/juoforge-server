package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface WorldView {

    Optional<UOMobile> getMobileBySerialId(int serial);

    List<UOItem> getItemsInRange(Location location, int radius);

    List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> filter);

    List<ItemTemplate> getItemsTemplate(String stockType);

    List<UOMobile> getMobilesInRange(Location location, int radius, Predicate<UOMobile> filter);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<UOContainer> getContainerBySerialId(int serial);

    List<Static> getStatics(int x, int y);

    List<Static> getStatics(Location location);

    LandTile getLandTile(int x, int y);

    LandTile getLandTile(Location location);

    List<UOPlayer> getOnlinePlayers();

    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);

    /**
     * Retrieves a region by its unique identifier.
     *
     * @param name the region unique identifier
     * @return an {@link Optional} containing the region if found, otherwise empty
     */
    Optional<RegionNode> getRegion(String name);

    /**
     * Resolves the most specific region that contains the given location.
     *
     * <p>
     * If multiple regions match, the implementation should return the
     * deepest region in the hierarchy (e.g., BUILDING over CITY,
     * DUNGEON_LEVEL over DUNGEON).
     *
     * @param location the world location to evaluate
     * @return an {@link Optional} containing the resolved region if any,
     *         otherwise empty if no region contains the location
     */
    Optional<RegionNode> getRegion(Location location);

    CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount uoAccount);
}
