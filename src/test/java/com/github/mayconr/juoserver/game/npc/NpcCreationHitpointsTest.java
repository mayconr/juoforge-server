package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.npc.NpcRequester;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.mobile.template.NpcStatProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.creation.CreateNpcStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.resolve.ResolveNpcStatsStep;
import com.github.mayconr.juoserver.game.npc.stats.NpcStats;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NpcCreationHitpointsTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final NpcStatProfile profile = new NpcStatProfile("basic", 60, 35, 20, 80, 70, 45);
    private final NpcStatsResolver resolver = new NpcStatsResolver(List.of(profile));

    @Test
    void createsSkeletonWithFullProfileResources() {
        var template = new JsonTemplateLoader<>(Path.of("content/npcs/monsters"), NpcTemplate.class)
                .load().get("skeleton");
        var profiles = new JsonTemplateLoader<>(Path.of("content/npc-profiles/stats"), NpcStatProfile.class).loadAll();
        var npc = create(template, new NpcStatsResolver(profiles));
        assertEquals("undead_basic", template.statProfile());
        assertNull(template.maxHitpoints());
        assertEquals(50, npc.getMaxHitpoints());
        assertEquals(npc.getMaxHitpoints(), npc.getHitpoints());
        assertEquals(50, npc.getMaxStamina());
        assertEquals(npc.getMaxStamina(), npc.getStamina());
        assertEquals(50, npc.getMaxMana());
        assertEquals(npc.getMaxMana(), npc.getMana());
        assertEquals(50, npc.getStrength());
        assertEquals(50, npc.getDexterity());
        assertEquals(50, npc.getIntelligence());
        assertTrue(npc.isAlive());
    }

    @Test
    void missingAndNullValuesUseProfile() throws Exception {
        for (var json : new String[]{
                "{\"name\":\"npc\",\"statProfile\":\"basic\"}",
                "{\"name\":\"npc\",\"statProfile\":\"basic\",\"maxHitpoints\":null,\"maxStamina\":null,\"maxMana\":null,\"strength\":null,\"dexterity\":null,\"intelligence\":null}"
        }) {
            var template = mapper.readValue(json, NpcTemplate.class);
            assertEquals(new NpcStats(60, 35, 20, 80, 70, 45), resolver.resolve(template));
            assertTrue(template.attr().isEmpty());
            assertTrue(template.equippedItems().isEmpty());
            assertTrue(template.roles().isEmpty());
        }
    }

    @Test
    void overridesAreIndependentAndDoNotMutateSharedProfile() throws Exception {
        var template = mapper.readValue(
                "{\"name\":\"elite\",\"statProfile\":\"basic\",\"maxHitpoints\":120,\"dexterity\":90}", NpcTemplate.class);
        var npc = create(template, resolver);
        assertEquals(120, npc.getHitpoints());
        assertEquals(120, npc.getMaxHitpoints());
        assertEquals(90, npc.getDexterity());
        assertEquals(60, npc.getStrength());
        assertEquals(20, npc.getIntelligence());
        assertEquals(70, npc.getStamina());
        assertEquals(45, npc.getMana());
        var ordinary = mapper.readValue("{\"name\":\"ordinary\",\"statProfile\":\"basic\"}", NpcTemplate.class);
        assertEquals(new NpcStats(60, 35, 20, 80, 70, 45), resolver.resolve(ordinary));
    }

    @Test
    void supportsFullyExplicitStatsWithoutProfile() throws Exception {
        var template = mapper.readValue(
                "{\"name\":\"custom\",\"strength\":60,\"dexterity\":35,\"intelligence\":20,\"maxHitpoints\":80,\"maxStamina\":70,\"maxMana\":45}",
                NpcTemplate.class);
        assertEquals(new NpcStats(60, 35, 20, 80, 70, 45), resolver.resolve(template));
    }

    @Test
    void rejectsUnknownProfileAndMissingStatsWithNpcName() throws Exception {
        var unknown = mapper.readValue("{\"name\":\"broken\",\"statProfile\":\"unknown\"}", NpcTemplate.class);
        var error = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(unknown));
        assertTrue(error.getMessage().contains("broken"));
        assertTrue(error.getMessage().contains("unknown"));
        var missing = mapper.readValue("{\"name\":\"incomplete\"}", NpcTemplate.class);
        error = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(missing));
        assertTrue(error.getMessage().contains("incomplete"));
        assertTrue(error.getMessage().contains("strength"));
    }

    @Test
    void rejectsNonPositiveOverrides() throws Exception {
        for (var field : new String[]{"maxHitpoints", "maxStamina", "maxMana", "strength", "dexterity", "intelligence"}) {
            for (var value : new int[]{0, -10}) {
                var template = mapper.readValue(
                        "{\"name\":\"invalid\",\"statProfile\":\"basic\",\"" + field + "\":" + value + "}", NpcTemplate.class);
                var error = assertThrows(IllegalArgumentException.class, () -> resolver.resolve(template));
                assertTrue(error.getMessage().contains(field));
                assertTrue(error.getMessage().contains("invalid"));
            }
        }
    }

    @Test
    void rejectsIncompleteAndDuplicateProfiles() {
        var error = assertThrows(com.fasterxml.jackson.databind.JsonMappingException.class,
                () -> mapper.readValue("{\"name\":\"incomplete\",\"strength\":50}", NpcStatProfile.class));
        assertTrue(error.getMessage().contains("incomplete"));
        assertThrows(IllegalArgumentException.class, () -> new NpcStatsResolver(List.of(profile, profile)));
    }

    @Test
    void bootstrapRejectsUnknownProfileBeforeStartingWorld() throws Exception {
        var shipped = com.github.mayconr.shard.ShardContentLoader.load(Path.of("."), false);
        var content = mock(com.github.mayconr.juoserver.WorldContent.class);
        when(content.items()).thenReturn(shipped.items());
        when(content.mounts()).thenReturn(shipped.mounts());
        when(content.npcStatProfiles()).thenReturn(shipped.npcStatProfiles());
        var invalid = mapper.readValue(
                "{\"name\":\"broken_spawn\",\"statProfile\":\"missing_profile\"}", NpcTemplate.class);
        when(content.npcs()).thenReturn(new com.github.mayconr.juoserver.infrastructure.template.TemplateLoader<>() {
            @Override
            public List<NpcTemplate> loadAll() {
                return List.of(invalid);
            }
        });
        var bootstrap = new com.github.mayconr.juoserver.WorldBootstrap(cfg -> cfg.content(content));
        var error = assertThrows(IllegalArgumentException.class, bootstrap::start);
        assertTrue(error.getMessage().contains("broken_spawn"));
        assertTrue(error.getMessage().contains("missing_profile"));
    }

    private UONpc create(NpcTemplate template, NpcStatsResolver statsResolver) {
        var storage = mock(RealmStorage.class);
        when(storage.createMobile(any(UOMobileData.class)))
                .thenAnswer(invocation -> new UONpc(invocation.getArgument(0, UOMobileData.class)));
        var context = new NpcCreationContext(new NpcRequester.AsyncProcess("test"), template.name(), mock(Location.class));
        context.setTemplate(template);
        context.setSerialId(42);
        new ResolveNpcStatsStep(statsResolver).execute(context);
        new CreateNpcStep(storage).execute(context);
        return context.getNpc();
    }
}
