package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.game.region.RegionModuleImpl;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionContext;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionFlowDefinition;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnContext;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnFlowDefinition;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.region.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerModuleTest {
    @Test
    void onlinePlayersReflectSessionsAndReturnIndependentSnapshot() {
        var storage = mock(RealmStorage.class);
        var events = new DefaultEventBus();
        PlayerModule module = new PlayerModuleImpl(storage, events);
        var city = new RegionNode("city", "City", null, new RectangularArea(0, 0, 100, 100));
        city.setAiActivationScope(true);
        var shop = new RegionNode("shop", "Shop", null, new RectangularArea(0, 0, 20, 20));
        city.addChild(shop);
        var regions = new RegionSystemImpl(Map.of("city", city, "shop", shop));
        var regionModule = new RegionModuleImpl(regions);
        var ai = mock(AIModule.class);
        var registry = new DefaultFlowRegistry();
        registry.register("regionTransition", RegionTransitionFlowDefinition.build(regions, events, regionModule, ai),
                RegionTransitionContext.class);
        registry.register("spawn", PlayerSpawnFlowDefinition.build(
                FlowRegistryFactory.GameModules.builder().region(regionModule).ai(ai).build(),
                FlowRegistryFactory.GameInfra.builder().regionSystem(regions).eventBus(events).build()),
                PlayerSpawnContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        regionModule.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        WorldPlayer players = module;
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(42);
        events.register(PlayerLoggedIn.class, event -> {
            assertEquals(List.of(player), module.getOnlinePlayers());
            assertEquals(Set.of(42), regionModule.getPlayerSerials(shop));
            assertEquals(Set.of(42), regionModule.getPlayerSerials(city));
            verify(ai).activateRegion(city);
        });

        assertTrue(players.getOnlinePlayers().isEmpty());
        module.spawn(player);
        verify(ai).activateRegion(city);
        var snapshot = players.getOnlinePlayers();
        assertEquals(List.of(player), snapshot);
        assertThrows(UnsupportedOperationException.class, snapshot::clear);

        module.despawn(player);
        assertTrue(players.getOnlinePlayers().isEmpty());
        assertEquals(List.of(player), snapshot);
        verify(storage).unloadMobile(player);
    }
}
