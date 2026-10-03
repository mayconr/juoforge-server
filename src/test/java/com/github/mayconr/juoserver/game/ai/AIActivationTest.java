package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.engine.AIEngine;
import com.github.mayconr.juoserver.game.ai.session.AISession;
import com.github.mayconr.juoserver.game.ai.session.AISessionImpl;
import com.github.mayconr.juoserver.game.ai.policy.AIActivationPolicy;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.event.MobileSpeech;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIActivationTest {
    @Test
    void pausesAndResumesWithoutAccumulatingInactiveTime() {
        var flows = mock(ModuleContext.FlowFacade.class);
        var context = new AIFlowContext(mock(UONpc.class), mock(World.class));
        var active = new AtomicBoolean(false);
        var session = new AISessionImpl<>(flows, context, action -> fail("Unexpected action"),
                ctx -> active.get(), (ctx, speech) -> true);
        session.onSpeech(new MobileSpeech(mock(UONpc.class), "hello", null));
        session.update(10);
        verifyNoInteractions(flows);
        assertNull(context.peekEvent(MobileSpeech.class));
        active.set(true);
        session.update(0.1);
        verify(flows).execute(context);
        assertEquals(0.1, context.delta());
    }

    @Test
    void revalidatesQueuedSpeechBeforeExecution() {
        var flows = mock(ModuleContext.FlowFacade.class);
        var context = new AIFlowContext(mock(UONpc.class), mock(World.class));
        var accepts = new AtomicBoolean(true);
        var session = new AISessionImpl<>(flows, context, action -> {},
                ctx -> true, (ctx, speech) -> accepts.get());
        session.onSpeech(new MobileSpeech(mock(UONpc.class), "buy", null));
        assertNotNull(context.peekEvent(MobileSpeech.class));
        accepts.set(false);
        session.update(0.1);
        assertNull(context.peekEvent(MobileSpeech.class));
    }

    @Test
    void combinesCustomRules() {
        AIActivationPolicy no = context -> false;
        AIActivationPolicy yes = context -> true;
        assertFalse(yes.and(no).isActive(null));
        assertTrue(no.or(yes).isActive(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    void registersOnceAndRemovesSpeechListenerOnDetach() {
        var engine = mock(AIEngine.class);
        var session = mock(AISession.class);
        var npc = mock(UONpc.class);
        when(engine.attach(npc)).thenReturn(session);
        var bus = new DefaultEventBus();
        var module = new AIModuleImpl(engine, bus);
        module.attach(npc);
        module.attach(npc);
        var speech = new MobileSpeech(npc, "hi", null);
        bus.publish(speech);
        verify(session).onSpeech(speech);
        module.detach(npc);
        bus.publish(speech);
        verifyNoMoreInteractions(session);
        module.attach(npc);
        module.detachAll();
        bus.publish(speech);
        verifyNoMoreInteractions(session);
    }
}
