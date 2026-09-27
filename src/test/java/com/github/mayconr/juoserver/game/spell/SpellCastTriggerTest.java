package com.github.mayconr.juoserver.game.spell;

import com.github.mayconr.juoserver.DefaultWorldCfg;
import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastContext;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastTrigger;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpellCastTriggerTest {
    private final SpellTemplate spell = new SpellTemplate("shard:nova", null, "Nova", Map.of("power", 10));
    private final UOMobile caster = mock(UOMobile.class);
    private final SpellCastContext context = new SpellCastContext(caster, spell);

    @Test
    void configuredFactoriesReceiveRuntimeAndOnlyFirstMatchingTriggerExecutes() {
        var cfg = new DefaultWorldCfg();
        var runtime = mock(ServerRuntime.class);
        var skipped = mock(SpellCastTrigger.class);
        var first = mock(SpellCastTrigger.class);
        var later = mock(SpellCastTrigger.class);
        cfg.addSpellTrigger(actual -> {
            assertSame(runtime, actual);
            return skipped;
        });
        cfg.addSpellTrigger(actual -> first);
        cfg.addSpellTrigger(actual -> later);
        var registry = new SpellCastRegistry();
        cfg.spellTriggerList().forEach(factory -> registry.register(factory.apply(runtime)));
        when(first.supports(context)).thenReturn(true);
        var module = SpellTestSupport.module(new InMemoryTemplateRegistry<>(List.of(spell), SpellTemplate::key), registry);

        module.castSpell(caster, spell.key());
        module.castSpell(caster, spell.key());

        verify(skipped, times(2)).supports(context);
        verify(skipped, never()).execute(any());
        verify(first, times(2)).execute(context);
        verifyNoInteractions(later);
    }

    @Test
    void unknownSpellDoesNotReachTriggers() {
        var registry = new SpellCastRegistry();
        var trigger = mock(SpellCastTrigger.class);
        registry.register(trigger);
        var module = SpellTestSupport.module(new InMemoryTemplateRegistry<>(List.of(spell), SpellTemplate::key), registry);
        module.castSpell(caster, "shard:unknown");
        verifyNoInteractions(trigger);
    }

    @Test
    void noMatchingTriggerReportsUnhandled() {
        var registry = new SpellCastRegistry();
        assertFalse(registry.dispatch(context));
        var trigger = mock(SpellCastTrigger.class);
        registry.register(trigger);
        assertFalse(registry.dispatch(context));
        verify(trigger, never()).execute(any());
    }

    @Test
    void triggerFailureDoesNotExecuteFallback() {
        var registry = new SpellCastRegistry();
        var first = mock(SpellCastTrigger.class);
        var fallback = mock(SpellCastTrigger.class);
        registry.register(first);
        registry.register(fallback);
        when(first.supports(context)).thenReturn(true);
        doThrow(new IllegalStateException("Failed spell")).when(first).execute(context);
        assertThrows(IllegalStateException.class, () -> registry.dispatch(context));
        verifyNoInteractions(fallback);
    }

    @Test
    void rejectsNullFactoriesAndTriggers() {
        assertThrows(NullPointerException.class, () -> new DefaultWorldCfg().addSpellTrigger(null));
        assertThrows(NullPointerException.class, () -> new SpellCastRegistry().register(null));
    }
}
