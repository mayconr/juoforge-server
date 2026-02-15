package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.EconomySystemInternal;
import com.github.mayconr.juoserver.game.economy.RegionStockEntry;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.player.PlayerSession;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplateRegistryInternal;
import com.github.mayconr.juoserver.network.packet.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView, MovementInternal, SpeechInternal, ItemInternal, ClickInternal,
        SkillInternal, CombatInternal, EconomySystemInternal, ItemTemplateRegistryInternal {

    void initialize();

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOMobile> unloadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<UOItem> unloadItem(int serialId);

    CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account);

    void login(UOPlayer player);

    void logout(UOPlayer player);

    PlayerSession getPlayerSession(UOMobile mobile);

    boolean isMobile(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    List<UOItem> getItemsInRange(Location location, int radius);

    void skillGained(UOMobile mobile, SkillValue value);

    void playerStatusRequested(UOPlayer sendTo, GetPlayerStatus getPlayerStatus);

    void tooltipRequest(UOPlayer player, List<Integer> serials);

    void resolveTarget(UOPlayer player, Target target);

    void sendBuyGump(UOPlayer player, UOMobile vendor, List<RegionStockEntry> items);

    void handleAction(UOPlayer player, ActionRequest request);

    void regen(UOMobile mobile, double interval);

    void gumpResponse(UOPlayer player, GumpSelection gumpSelection);
}
