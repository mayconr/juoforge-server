package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.game.session.player.PlayerSession;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import com.github.mayconr.juoserver.network.packet.GetPlayerStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WorldInternal extends WorldActions, WorldView, MovementInternal, SpeechInternal, ItemInternal {

    void initialize();

    CompletableFuture<UOMobile> loadMobile(int serialId);

    CompletableFuture<UOMobile> unloadMobile(int serialId);

    CompletableFuture<UOItem> loadItem(int serialId);

    CompletableFuture<UOItem> unloadItem(int serialId);

    CompletableFuture<PlayerSession> createAndLoginPlayer(CreateCharacter character, SessionOutbound outbound);

    CompletableFuture<PlayerSession> loginExistingPlayer(UOPlayer player, SessionOutbound outbound);

    PlayerSession getPlayerSession(UOMobile mobile);

    boolean isMobile(int serialId);

    CompletableFuture<List<UOItem>> loadContainerItems(Container container);

    List<UOItem> getItemsInRange(Location location, int radius);

    void dropItemOnTheGround(UOItem item);

    void removeItemFromTheGround(UOItem item);

    void skillGained(UOMobile mobile, SkillValue value);

    void playerStatusRequested(UOPlayer sendTo, GetPlayerStatus getPlayerStatus);

}
