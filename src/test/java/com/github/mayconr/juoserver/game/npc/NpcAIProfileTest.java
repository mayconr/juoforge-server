package com.github.mayconr.juoserver.game.npc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.mobile.template.NpcAIProfile;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.ai.NpcAIProfileResolver;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.creation.CreateNpcStep;
import com.github.mayconr.juoserver.game.npc.flow.creation.resolve.ResolveNpcAIProfileStep;
import com.github.mayconr.juoserver.game.npc.stats.NpcStatsResolver;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.shard.ShardContentLoader;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NpcAIProfileTest {
    @Test
    void readsLegacyPersistedBehaviorAndWritesWithoutRetiredField() throws Exception {
        var handler = new com.github.mayconr.shard.storage.types.BehaviorDefinitionTypeHandler();
        var resultSet = mock(java.sql.ResultSet.class);
        when(resultSet.getString("behavior")).thenReturn("""
                {"profile":"VENDOR","ai":"DIALOGUE_REACTIVE","radius":3,"supports":["buy"],"stockType":"ORE"}
                """);
        var behavior = handler.getNullableResult(resultSet, "behavior");
        assertEquals("DIALOGUE_REACTIVE", behavior.ai());
        assertEquals(3, behavior.speechRadius());
        assertEquals(List.of("buy"), behavior.speechTriggers());
        assertNull(new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(behavior)).get("stockType"));
        var statement = mock(java.sql.PreparedStatement.class);
        handler.setNonNullParameter(statement, 1, behavior, org.apache.ibatis.type.JdbcType.OTHER);
        var json = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(statement).setString(eq(1), json.capture());
        var mapper = new ObjectMapper();
        assertFalse(mapper.readTree(json.getValue()).has("profile"));
        assertFalse(mapper.readTree(json.getValue()).has("supports"));
        assertEquals("buy", mapper.readTree(json.getValue()).get("speechTriggers").get(0).asText());
        assertEquals(behavior, mapper.readValue(json.getValue(),
                com.github.mayconr.juoserver.game.model.BehaviorDefinition.class));
    }

    @Test
    void legacyProfileFilesRemainReadableAndOtherUnknownFieldsStillFail() throws Exception {
        var mapper = new ObjectMapper();
        var profile = mapper.readValue("""
                {"name":"legacy","profile":"ANIMAL","ai":"PASSIVE_ANIMAL","radius":3,"supports":["hello"]}
                """, NpcAIProfile.class);
        assertEquals("PASSIVE_ANIMAL", profile.behavior().ai());
        assertEquals(List.of("hello"), profile.speechTriggers());
        assertFalse(mapper.readTree(mapper.writeValueAsString(profile)).has("profile"));
        assertThrows(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException.class,
                () -> mapper.readValue("{\"ai\":\"AGGRESSIVE\",\"typo\":1}",
                        com.github.mayconr.juoserver.game.model.BehaviorDefinition.class));
    }

    @Test
    void allShippedProfilesResolveAndCreationPersistsExplicitParameters() {
        var content = ShardContentLoader.load(Path.of("."), false);
        var resolver = new NpcAIProfileResolver(content.npcAIProfiles().loadAll());
        var stats = new NpcStatsResolver(content.npcStatProfiles().loadAll());
        var storage = mock(RealmStorage.class);
        when(storage.createMobile(any())).thenAnswer(inv -> new UONpc(inv.getArgument(0, UOMobileData.class)));
        for (var template : content.npcs().loadAll()) {
            assertNotNull(template.aiProfile());
            assertTrue(template.attr().keySet().stream().noneMatch(key -> key.startsWith("behavior.")));
            var context = new NpcCreationContext(new NpcRequester.AsyncProcess("test"),
                    template.name(), mock(Location.class));
            context.setTemplate(template);
            context.setSerialId(42);
            context.setStats(stats.resolve(template));
            new ResolveNpcAIProfileStep(resolver).execute(context);
            new CreateNpcStep(storage).execute(context);
            assertEquals(resolver.resolve(template), context.getNpc().getBehavior());
            assertEquals(template.stockType(), context.getNpc().getStockType());
            assertEquals(template.stockType(), context.getNpc().toData().getStockType());
            assertEquals(template.stockType(), new UONpc(context.getNpc().toData()).getStockType());
            assertEquals(24, context.getNpc().getBehavior().activationRadius());
            if (!template.aiProfile().startsWith("vendor") && !template.aiProfile().startsWith("banker")) {
                assertTrue(context.getNpc().getBehavior().speechTriggers().isEmpty());
                assertNull(context.getNpc().getBehavior().speechRadius());
            }
            if (template.name().equals("blacksmith")) {
                assertEquals("ORE", context.getNpc().getStockType());
                assertEquals("NPC_BLACKSMITH", template.attr().get("speech.messageStyle"));
            }
        }
    }

    @Test
    void inlineOverridesPreserveOtherProfileValues() throws Exception {
        var profile = new NpcAIProfile("vendor", "DIALOGUE_REACTIVE", 24, null, 3, List.of("buy"));
        var resolver = new NpcAIProfileResolver(List.of(profile));
        var template = new ObjectMapper().readValue("""
                {"name":"custom","aiProfile":"vendor","stockType":"TOOLS","behavior":{"speechRadius":8,"speechTriggers":[]}}
                """, NpcTemplate.class);
        var result = resolver.resolve(template);
        assertEquals(8, result.speechRadius());
        assertEquals(24, result.activationRadius());
        assertTrue(result.speechTriggers().isEmpty());
        assertEquals("TOOLS", template.stockType());
        assertEquals("DIALOGUE_REACTIVE", result.ai());
        assertEquals(3, profile.speechRadius());
    }

    @Test
    void rejectsUnknownAndDuplicateProfiles() throws Exception {
        var profile = new NpcAIProfile("basic", "PASSIVE_ANIMAL", 24, null, null, List.of());
        assertThrows(IllegalArgumentException.class, () -> new NpcAIProfileResolver(List.of(profile, profile)));
        var template = new ObjectMapper().readValue("{\"name\":\"broken\",\"aiProfile\":\"missing\"}", NpcTemplate.class);
        var error = assertThrows(IllegalArgumentException.class,
                () -> new NpcAIProfileResolver(List.of(profile)).resolve(template));
        assertTrue(error.getMessage().contains("broken"));
        assertTrue(error.getMessage().contains("missing"));
    }
}
