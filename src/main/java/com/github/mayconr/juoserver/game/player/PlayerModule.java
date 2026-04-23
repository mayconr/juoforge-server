package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedOut;
import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.CreateCharacter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class PlayerModule implements WorldModule, PlayerCommands {

    private final Map<Integer, UOPlayer> onlinePlayers = new ConcurrentHashMap<>();
    private final PlayerVitalsHandler playerVitalsHandler;
    private final RealmStorage storage;
    private final EventBus eventBus;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void update(double delta) {
        for (UOPlayer player : onlinePlayers.values()) {
            playerVitalsHandler.update(player, delta);
        }
    }

    @Override
    public CompletableFuture<UOPlayer> createPlayerMobile(CreateCharacter character, Map<Integer, RegionNode> startingLocations, UOAccount account) {
        final var context = new PlayerCreationContext(character, startingLocations, account);
        flows.execute(context);
        return context.result();
    }

    @Override
    public void spawn(UOPlayer player) {
        onlinePlayers.put(player.getSerialId(), player);

        eventBus.publish(new PlayerLoggedIn(player));
    }

    @Override
    public void despawn(UOPlayer player) {
        storage.unloadMobile(player);
        onlinePlayers.remove(player.getSerialId());

        eventBus.publish(new PlayerLoggedOut(player));
    }

    @Override
    public CompletableFuture<Void> deletePlayerMobile(int serialId) {
        return storage.deleteMobile(serialId).whenComplete((player, throwable) -> {
            if (throwable != null) {
                log.error("Error while deleting player with id {}", serialId, throwable);
            }
        });
    }

    @Override
    public List<UOPlayer> getOnlinePlayers() {
        return List.copyOf(onlinePlayers.values());
    }
}
