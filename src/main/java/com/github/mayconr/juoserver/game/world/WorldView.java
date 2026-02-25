package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RegionQueryService;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface WorldView extends RegionQueryService {

    Optional<UOMobile> getMobileBySerialId(int serial);

    List<UOItem> getItemsInRange(Location location, int radius);

    List<UOItem> getItemsInContainer(Container container, Predicate<UOItem> filter);

    List<ItemTemplate> getItemsTemplate(String stockType);

    List<UOMobile> getMobilesInRange(Location location, int radius);

    Optional<UOItem> getItemBySerialId(int serial);

    Optional<Container> getContainerBySerialId(int serial);

    List<Static> getStatics(int x, int y);

    List<Static> getStatics(Location location);

    LandTile getLandTile(int x, int y);

    LandTile getLandTile(Location location);

    List<UOPlayer> getOnlinePlayers();

    boolean isInRange(Location object1, Location object2, int radius);

    Optional<StockEntry> getStockEntry(ItemTemplate template, RegionNode regionNode);

    Optional<RegionNode> getRegionNode(Location location);
}
