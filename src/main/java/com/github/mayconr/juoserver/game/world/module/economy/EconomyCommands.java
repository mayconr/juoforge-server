package com.github.mayconr.juoserver.game.world.module.economy;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;

import java.util.List;

public interface EconomyCommands {

    void sendBuyGump(UOPlayer player, UOMobile vendor, RegionNode region, List<RegionStockEntry> items);
}
