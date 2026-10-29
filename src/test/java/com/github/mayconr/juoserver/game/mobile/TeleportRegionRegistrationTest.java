package com.github.mayconr.juoserver.game.mobile;

import com.github.mayconr.juoserver.game.ai.AIModule;
import com.github.mayconr.juoserver.game.mobile.flow.teleport.*;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.region.RegionModuleImpl;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionContext;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionFlowDefinition;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.region.*;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleportRegionRegistrationTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void registersPlayerBeforeNotificationAndActivatesAiOnRegionEntry(boolean sameRegion) {
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(42);
        var position = new AtomicReference<Location>(new PointInTheWorld(sameRegion ? 5 : 200, 5, 0));
        when(player.getX()).thenAnswer(call -> position.get().getX());
        when(player.getY()).thenAnswer(call -> position.get().getY());
        when(player.getZ()).thenAnswer(call -> position.get().getZ());
        doAnswer(call -> { position.set(call.getArgument(0)); return null; })
                .when(player).setLocation(any(Location.class));
        var city = new RegionNode("city", "City", null, new RectangularArea(0, 0, 100, 100));
        city.setAiActivationScope(true);
        var shop = new RegionNode("shop", "Shop", null, new RectangularArea(0, 0, 20, 20));
        city.addChild(shop);
        var regions = new RegionSystemImpl(Map.of("city", city, "shop", shop));
        var regionModule = new RegionModuleImpl(regions);
        var ai = mock(AIModule.class);
        doAnswer(call -> {
            assertEquals(Set.of(42), regionModule.getPlayerSerials(shop));
            assertEquals(Set.of(42), regionModule.getPlayerSerials(city));
            return null;
        }).when(ai).activateRegion(city);
        var storage = mock(RealmStorage.class);
        var events = new DefaultEventBus();
        var published = new java.util.ArrayList<MobileMoved>();
        events.register(MobileMoved.class, event -> {
            assertEquals(Set.of(42), regionModule.getPlayerSerials(shop));
            assertEquals(Set.of(42), regionModule.getPlayerSerials(city));
            verify(ai, times(sameRegion ? 0 : 1)).activateRegion(city);
            published.add(event);
        });
        var registry = new DefaultFlowRegistry();
        registry.register("regionTransition", RegionTransitionFlowDefinition.build(regions, events, regionModule, ai, mock(com.github.mayconr.juoserver.game.mobile.WorldMobile.class)),
                RegionTransitionContext.class);
        regionModule.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        registry.register("teleport", TeleportFlowDefinition.build(
                FlowRegistryFactory.GameModules.builder().region(regionModule).ai(ai).build(),
                FlowRegistryFactory.GameInfra.builder().regionSystem(regions).storage(storage)
                        .eventBus(events).fileReader(mock(UOFileReader.class)).build()),
                TeleportContext.class);
        var destination = new PointInTheWorld(10, 10, 0);

        var result = new DefaultFlowFacade(registry).execute(TeleportContext.of(player, destination));

        assertTrue(result.flowSucceeded());
        assertEquals(destination, position.get());
        verify(ai, times(sameRegion ? 0 : 1)).activateRegion(city);
        assertEquals(1, published.size());
        assertEquals(new PointInTheWorld(sameRegion ? 5 : 200, 5, 0), published.getFirst().from());
        assertEquals(destination, published.getFirst().to());
        assertTrue(published.getFirst().teleport());
        verify(storage).updateMobileLocation(player, new PointInTheWorld(sameRegion ? 5 : 200, 5, 0), destination);
    }
}
