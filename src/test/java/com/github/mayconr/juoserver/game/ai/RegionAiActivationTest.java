package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.engine.AIEngine;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionContext;
import com.github.mayconr.juoserver.game.ai.flow.activation.ActivateRegionFlowDefinition;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.region.flow.transition.UpdateRegionAiStep;
import com.github.mayconr.juoserver.game.region.flow.transition.RegionTransitionContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.infrastructure.region.RectangularArea;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystemImpl;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.*;

class RegionAiActivationTest {
    @Test
    void playerEnteringMicroregionActivatesLivingAttachedNpcsInParentArea() {
        var engine = mock(AIEngine.class);
        var bus = new DefaultEventBus();
        var module = new AIModuleImpl(engine, bus);
        var registry = new DefaultFlowRegistry();
        registry.register("activateRegion", ActivateRegionFlowDefinition.build(module), ActivateRegionContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        var scope = new RegionNode("city", "City", null, new RectangularArea(0, 0, 100, 100));
        scope.setAiActivationScope(true);
        var microregion = new RegionNode("shop", "Shop", null, new RectangularArea(0, 0, 20, 20));
        scope.addChild(microregion);
        var regions = new RegionSystemImpl(Map.of("city", scope, "shop", microregion));
        var inside = attach(module, engine, 1, 10, true);
        var outside = attach(module, engine, 2, 200, true);
        var dead = attach(module, engine, 3, 10, false);
        var detached = attach(module, engine, 4, 10, true);
        module.detach(detached);

        transition(module, regions, mock(UOPlayer.class), microregion);

        verify(engine).activate(inside);
        verify(engine, never()).activate(outside);
        verify(engine, never()).activate(dead);
        verify(engine, never()).activate(detached);
    }

    @Test
    void ignoresNpcEntriesAndRegionsWithoutActivationScope() {
        var module = mock(AIModule.class);
        var region = new RegionNode("world", "World", null, new RectangularArea(0, 0, 100, 100));
        var regions = new RegionSystemImpl(Map.of("world", region));
        transition(module, regions, mock(UOPlayer.class), region);
        region.setAiActivationScope(true);
        transition(module, regions, mock(UONpc.class), region);

        verifyNoInteractions(module);
    }

    private void transition(AIModule module, RegionSystemImpl regions, UOMobile mobile, RegionNode destination) {
        var context = new RegionTransitionContext(mobile, null, null);
        context.setOldRegion(new RegionNode("old", "Old", null, null));
        context.setNewRegion(destination);
        new UpdateRegionAiStep(regions, module).execute(context);
    }

    @SuppressWarnings("unchecked")
    private UONpc attach(AIModule module, AIEngine engine, int serial, int x, boolean alive) {
        var npc = mock(UONpc.class);
        when(npc.getSerialId()).thenReturn(serial);
        when(npc.getX()).thenReturn(x);
        when(npc.isAlive()).thenReturn(alive);
        when(engine.attach(npc)).thenReturn(mock(AISession.class));
        module.attach(npc);
        return npc;
    }
}
