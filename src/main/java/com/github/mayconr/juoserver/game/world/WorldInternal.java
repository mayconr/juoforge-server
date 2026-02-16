package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.world.module.economy.EconomySystemInternal;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.module.item.template.ItemTemplateRegistryInternal;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView, MovementInternal, SpeechInternal, ItemInternal, ClickInternal,
        SkillInternal, CombatInternal, EconomySystemInternal, ItemTemplateRegistryInternal, UiInternal, SessionInternal {

    void initialize();

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOMobile> unloadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<UOItem> unloadItem(int serialId);

    CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account);

    boolean isMobile(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    void handleAction(UOPlayer player, ActionRequest request);

}
