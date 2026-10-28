package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.DefaultWorldCfg;
import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.engine.AIEngineImpl;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AILifecycleTest {
    @Test
    void attachWaitsForActivationAndRepeatedActivationPreservesSession() {
        var cfg = spy(new DefaultWorldCfg());
        cfg.aiActivationPolicy(() -> context -> true);
        cfg.aiSpeechPolicy(() -> (context, speech) -> true);
        doReturn(new GamePlaySettings.Ai(0.25)).when(cfg).ai();
        var flows = mock(ModuleContext.FlowFacade.class);
        var bus = new DefaultEventBus();
        var engine = new AIEngineImpl(mock(World.class), action -> fail("Unexpected action"), cfg);
        var module = new AIModuleImpl(engine, bus);
        engine.initialize(flows);
        var npc = mock(UONpc.class, RETURNS_DEEP_STUBS);
        when(npc.getBehavior().ai()).thenReturn("PASSIVE_ANIMAL");
        var session = module.attach(npc);
        var speech = new MobileSpeech(npc, "hello", null);
        bus.publish(speech);
        module.update(0.25);
        verifyNoInteractions(flows);
        assertEquals(new AIStatus("PASSIVE_ANIMAL", true, false), module.getAiStatus(npc));

        module.activate(npc);
        module.activate(npc);
        assertSame(session, module.attach(npc));
        doAnswer(invocation -> {
            AIFlowContext context = invocation.getArgument(0);
            assertNull(context.peekEvent(MobileSpeech.class));
            context.set("preserved", true);
            return null;
        }).when(flows).execute(any(AIFlowContext.class));
        module.update(0.25);
        verify(flows).execute(any(AIFlowContext.class));

        module.activate(npc);
        bus.publish(speech);
        doAnswer(invocation -> {
            AIFlowContext context = invocation.getArgument(0);
            assertEquals(Boolean.TRUE, context.get("preserved", Boolean.class));
            assertSame(speech, context.pollEvent(MobileSpeech.class));
            assertNull(context.pollEvent(MobileSpeech.class));
            return null;
        }).when(flows).execute(any(AIFlowContext.class));
        module.update(0.25);
        verify(flows, times(2)).execute(any(AIFlowContext.class));
        module.detach(npc);
        assertEquals(new AIStatus(null, false, false), module.getAiStatus(npc));
        assertThrows(IllegalStateException.class, () -> module.activate(npc));
        bus.publish(speech);
        module.update(0.25);
        verifyNoMoreInteractions(flows);
    }
}
