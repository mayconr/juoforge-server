package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.economy.EconomySystemInternal;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistryInternal;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOCity;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.network.packet.ActionRequest;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView, MovementInternal, SpeechInternal, ItemInternal, ClickInternal,
        SkillInternal, CombatInternal, EconomySystemInternal, ItemTemplateRegistryInternal, UiInternal, SessionInternal {

    void initialize();

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOPlayer> createPlayer(CreateCharacter character, Map<Integer, UOCity> cities, UOAccount account);

    boolean isMobile(int serialId);

    void handleAction(UOPlayer player, ActionRequest request);

}
