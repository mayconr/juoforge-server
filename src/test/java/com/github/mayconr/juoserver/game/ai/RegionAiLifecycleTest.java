package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.DefaultWorldCfg;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.engine.AIEngineImpl;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionContext;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionFlowDefinition;
import com.github.mayconr.juoserver.game.ai.flow.deactivation.DeactivateRegionContext;
import com.github.mayconr.juoserver.game.ai.flow.deactivation.DeactivateRegionFlowDefinition;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.region.RegionModuleImpl;
import com.github.mayconr.juoserver.game.player.PlayerModuleImpl;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnContext;
import com.github.mayconr.juoserver.game.player.flow.spawn.PlayerSpawnFlowDefinition;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.game.region.flow.transition.*;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.region.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegionAiLifecycleTest {
    @Test
    void movementActivatesKeepsActiveUntilLastExitAndReactivatesSameSession() {
        var city = new RegionNode("city", "City", RegionType.CITY, new RectangularArea(0, 0, 100, 100));
        city.setAiActivationScope(true);
        var shop = new RegionNode("shop", "Shop", RegionType.BUILDING, new RectangularArea(0, 0, 20, 20));
        city.addChild(shop);
        var regions = new RegionSystemImpl(Map.of("city", city, "shop", shop));
        var regionModule = new RegionModuleImpl(regions);
        var cfg = spy(new DefaultWorldCfg());
        cfg.aiActivationPolicy(() -> context -> true);
        cfg.aiSpeechPolicy(() -> (context, speech) -> true);
        doReturn(new GamePlaySettings.Ai(0.25)).when(cfg).ai();
        var aiFlows = mock(ModuleContext.FlowFacade.class);
        var bus = new DefaultEventBus();
        var engine = new AIEngineImpl(mock(World.class), action -> fail("Unexpected action"), cfg);
        engine.initialize(aiFlows);
        var ai = new AIModuleImpl(engine, bus);
        var npc = mock(UONpc.class, RETURNS_DEEP_STUBS);
        when(npc.getSerialId()).thenReturn(100);
        when(npc.isAlive()).thenReturn(true);
        when(npc.getX()).thenReturn(10);
        when(npc.getBehavior().ai()).thenReturn("PASSIVE_ANIMAL");
        var session = ai.attach(npc);
        var registry = new DefaultFlowRegistry();
        var players = new PlayerModuleImpl(mock(RealmStorage.class), bus);
        registry.register("spawn", PlayerSpawnFlowDefinition.build(
                FlowRegistryFactory.GameModules.builder().region(regionModule).ai(ai).build(),
                FlowRegistryFactory.GameInfra.builder().regionSystem(regions).eventBus(bus).build()),
                PlayerSpawnContext.class);
        registry.register("activateRegion", ActivateRegionFlowDefinition.build(ai), ActivateRegionContext.class);
        registry.register("regionTransition", RegionTransitionFlowDefinition.build(regions, bus, regionModule, ai),
                RegionTransitionContext.class);
        registry.register("deactivateRegion", DeactivateRegionFlowDefinition.build(regionModule, ai),
                DeactivateRegionContext.class);
        var facade = new DefaultFlowFacade(registry);
        players.initialize(new DefaultModuleContext(facade));
        when(aiFlows.execute(any(ActivateRegionContext.class)))
                .thenAnswer(call -> facade.execute(call.<ActivateRegionContext>getArgument(0)));
        when(aiFlows.execute(any(DeactivateRegionContext.class)))
                .thenAnswer(call -> facade.execute(call.<DeactivateRegionContext>getArgument(0)));
        ai.initialize(new DefaultModuleContext(aiFlows));
        regionModule.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        bus.register(MobileMoved.class,
                event -> regionModule.regionTransition(event.mobile(), event.from(), event.to()));
        var first = mock(UOPlayer.class);
        var second = mock(UOPlayer.class);
        when(first.getSerialId()).thenReturn(1);
        when(second.getSerialId()).thenReturn(2);
        var outside = new PointInTheWorld(200, 0, 0);
        var inside = new PointInTheWorld(10, 0, 0);
        var street = new PointInTheWorld(50, 0, 0);

        ai.update(0.25);
        verify(aiFlows, never()).execute(any(AIFlowContext.class));
        players.spawn(first);
        ai.update(0.25);
        verify(aiFlows).execute(any(AIFlowContext.class));
        clearInvocations(aiFlows);
        move(bus, second, outside, inside);
        move(bus, first, inside, street);
        move(bus, first, street, outside);
        ai.update(0.25);
        verify(aiFlows).execute(any(AIFlowContext.class));
        clearInvocations(aiFlows);
        bus.publish(new MobileSpeech(npc, "queued", null));
        move(bus, second, inside, outside);
        assertTrue(regionModule.getPlayerSerials(city).isEmpty());
        bus.publish(new MobileSpeech(npc, "inactive", null));
        ai.update(10);
        verify(aiFlows, never()).execute(any(AIFlowContext.class));
        move(bus, first, outside, inside);
        assertSame(session, ai.attach(npc));
        doAnswer(call -> {
            assertNull(call.<AIFlowContext>getArgument(0).peekEvent(MobileSpeech.class));
            return null;
        }).when(aiFlows).execute(any(AIFlowContext.class));
        ai.update(0.25);
        verify(aiFlows).execute(any(AIFlowContext.class));
    }

    private void move(DefaultEventBus bus, UOPlayer player, Location from, Location to) {
        bus.publish(new MobileMoved(player, null, from, to, false, 0, false));
    }
}
