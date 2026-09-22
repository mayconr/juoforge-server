package com.github.mayconr.juoserver.game.spell;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.spell.template.SpellTemplate;
import com.github.mayconr.juoserver.game.spell.trigger.SpellCastRegistry;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoaderNew;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpellModuleTest {
    @Test
    void loadsShardTemplatesAndLogsKnownAndUnknownSpellsWithoutMutatingCaster() {
        var templates = new JsonTemplateLoaderNew<>(Path.of("template/spells/spells.json"), SpellTemplate.class).loadAll();
        var module = new SpellModuleImpl(new InMemoryTemplateRegistry<>(templates, SpellTemplate::key), new SpellCastRegistry());
        assertEquals(64, templates.size());
        for (var template : templates) {
            assertEquals(template, module.getSpellByClientId(template.clientSpellId()).orElseThrow());
        }
        var caster = mock(UOMobile.class);
        when(caster.getName()).thenReturn("Caster");
        when(caster.getSerialId()).thenReturn(42);
        var logger = (Logger) LoggerFactory.getLogger(SpellModuleImpl.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            module.castSpell(caster, "magery:clumsy");
            module.castSpell(caster, "shard:unknown");
            assertEquals(3, appender.list.size());
            var known = appender.list.getFirst().getFormattedMessage();
            assertTrue(known.contains("Clumsy (key=magery:clumsy)"));
            assertTrue(known.contains("school=magery"));
            assertTrue(appender.list.get(1).getFormattedMessage().contains("No spell cast trigger registered"));
            assertTrue(appender.list.getLast().getFormattedMessage().contains("Unknown spell key: shard:unknown"));
            verify(caster, times(2)).getName();
            verify(caster, times(2)).getSerialId();
            verifyNoMoreInteractions(caster);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void rejectsDuplicateSpellIds() {
        var templates = List.of(new SpellTemplate("shard:first", 1, "First", null), new SpellTemplate("shard:second", 1, "Second", null));
        assertThrows(IllegalArgumentException.class,
                () -> new SpellModuleImpl(new InMemoryTemplateRegistry<>(templates, SpellTemplate::key), new SpellCastRegistry()));
    }

    @Test
    void validatesTemplateIdentityAndDefaultsMetadata() {
        assertThrows(IllegalArgumentException.class, () -> new SpellTemplate("invalid", null, "Invalid", null));
        assertThrows(IllegalArgumentException.class, () -> new SpellTemplate("shard:invalid", -1, "Invalid", null));
        assertThrows(IllegalArgumentException.class, () -> new SpellTemplate("shard:invalid", 65536, "Invalid", null));
        assertThrows(IllegalArgumentException.class, () -> new SpellTemplate("shard:invalid", 1, " ", null));
        assertTrue(new SpellTemplate("shard:valid", null, "Valid", null).metadata().isEmpty());
    }

    @Test
    void rejectsDuplicateKeysEvenWithoutClientIds() {
        var templates = List.of(
                new SpellTemplate("shard:nova", null, "Nova", null),
                new SpellTemplate("shard:nova", null, "Another Nova", null));
        assertThrows(IllegalArgumentException.class,
                () -> new SpellModuleImpl(new InMemoryTemplateRegistry<>(templates, SpellTemplate::key), new SpellCastRegistry()));
    }

    @Test
    void customSpellsResolveByKeyWithoutClientMapping() {
        var templates = List.of(
                new SpellTemplate("shard:arcane_nova", null, "Arcane Nova", null),
                new SpellTemplate("shard:other", null, "Other", null));
        var module = new SpellModuleImpl(new InMemoryTemplateRegistry<>(templates, SpellTemplate::key), new SpellCastRegistry());
        assertTrue(module.getSpellByClientId(10000).isEmpty());
        var caster = mock(UOMobile.class);
        var logger = (Logger) LoggerFactory.getLogger(SpellModuleImpl.class);
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            module.castSpell(caster, "shard:arcane_nova");
            assertEquals(2, appender.list.size());
            assertTrue(appender.list.getFirst().getFormattedMessage().contains("Arcane Nova (key=shard:arcane_nova)"));
            verify(caster).getName();
            verify(caster).getSerialId();
            verifyNoMoreInteractions(caster);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
