package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellContext;
import com.github.mayconr.juoserver.game.spell.flow.cast.CastSpellFlowDefinition;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastTrigger;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CastSpellFlowTest {
    @Test
    void invalidRequestsStopBeforeTemplateResolutionOrDispatch() {
        @SuppressWarnings("unchecked")
        TemplateRegistry<String, SpellTemplate> templates = mock(TemplateRegistry.class);
        var triggers = mock(SpellCastRegistry.class);
        var flow = CastSpellFlowDefinition.build(templates, triggers);
        var caster = mock(UOMobile.class);
        assertTrue(flow.execute(new CastSpellContext(null, "magery:heal")).flowFailed());
        assertTrue(flow.execute(new CastSpellContext(caster, null)).flowFailed());
        assertTrue(flow.execute(new CastSpellContext(caster, " ")).flowFailed());
        verifyNoInteractions(templates, triggers);
    }

    @Test
    void triggerExceptionFailsFlowWithoutExecutingFallback() {
        var spell = new SpellTemplate("magery:heal", 4, "Heal", null);
        var templates = new InMemoryTemplateRegistry<>(List.of(spell), SpellTemplate::key);
        var triggers = new SpellCastRegistry();
        var first = mock(SpellCastTrigger.class);
        var fallback = mock(SpellCastTrigger.class);
        triggers.register(first);
        triggers.register(fallback);
        when(first.supports(any())).thenReturn(true);
        doThrow(new IllegalStateException("Failed spell")).when(first).execute(any());
        var flow = CastSpellFlowDefinition.build(templates, triggers);
        var context = new CastSpellContext(mock(UOMobile.class), spell.key());
        var result = flow.execute(context);
        assertTrue(result.flowFailed());
        assertEquals("FLOW_EXCEPTION", result.code());
        assertSame(spell, context.getSpell());
        verifyNoInteractions(fallback);
    }
}
