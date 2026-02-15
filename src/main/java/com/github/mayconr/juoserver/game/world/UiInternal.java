package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;
import com.github.mayconr.juoserver.network.packet.GumpSelection;
import com.github.mayconr.juoserver.network.packet.Target;

import java.util.List;

public interface UiInternal {
    void tooltipRequest(UOPlayer player, List<Integer> serials);

    void resolveTarget(UOPlayer player, Target target);

    void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items);

    void playerStatusRequested(UOPlayer sendTo, GetPlayerStatus getPlayerStatus);

    void gumpResponse(UOPlayer player, GumpSelection gumpSelection);
}
