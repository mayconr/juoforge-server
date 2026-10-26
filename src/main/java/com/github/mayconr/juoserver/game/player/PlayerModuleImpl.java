package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.player.flow.creation.PlayerCreationContext;
import com.github.mayconr.juoserver.game.player.flow.deletion.DeletePlayerContext;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.AccountMobile;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedOut;
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
public class PlayerModuleImpl implements PlayerModule {

    private final Map<Integer, UOPlayer> onlinePlayers = new ConcurrentHashMap<>();
    private final RealmStorage storage;
    private final EventBus eventBus;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
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
        var context = new DeletePlayerContext(serialId);
        try {
            var result = flows.execute(context);
            if (result.flowFailed() && !context.result().isDone()) {
                context.fail(new IllegalStateException(result.reason()));
            }
        } catch (RuntimeException error) {
            context.fail(error);
        }
        return context.result().whenComplete((unused, throwable) -> {
            if (throwable != null) {
                log.error("Error while deleting player with id {}", serialId, throwable);
            }
        });
    }

    @Override
    public List<UOPlayer> getOnlinePlayers() {
        return List.copyOf(onlinePlayers.values());
    }

    @Override
    public CompletableFuture<List<AccountMobile>> getPlayerMobiles(UOAccount account) {
        return storage.getAccountMobiles(account);
    }

    @Override
    public CompletableFuture<UOMobile> loadMobile(int serialId) {
        if (!UOMobile.isMobile(serialId)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Serial [" + serialId + "] is not a player"));
        }
        return storage.loadMobile(serialId);
    }
}
