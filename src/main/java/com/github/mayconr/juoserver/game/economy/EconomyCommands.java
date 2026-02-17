package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.stock.RegionStockEntry;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.List;

public interface EconomyCommands {

    void sendBuyGump(UOPlayer player, UOMobile vendor, RegionNode region, List<RegionStockEntry> items);
}
