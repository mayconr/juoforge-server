package com.github.mayconr.juoserver.game.npc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.mobile.template.NpcTemplate;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.creation.creation.CreateNpcStep;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NpcCreationHitpointsTest {
    @Test
    void createsSkeletonWithFullTemplateHitpoints() {
        var template = new JsonTemplateLoader<>(Path.of("content/npcs/monsters"), NpcTemplate.class)
                .load().get("skeleton");
        var npc = create(template);
        assertEquals(50, npc.getMaxHitpoints());
        assertEquals(npc.getMaxHitpoints(), npc.getHitpoints());
        assertEquals(50, npc.getMaxStamina());
        assertEquals(npc.getMaxStamina(), npc.getStamina());
        assertEquals(50, npc.getMaxMana());
        assertEquals(npc.getMaxMana(), npc.getMana());
        assertEquals(50, npc.getDexterity());
        assertEquals(50, npc.getStrength());
        assertEquals(50, npc.getIntelligence());
        assertTrue(npc.isAlive());
    }

    @Test
    void defaultsMissingAndNullMaximumWithoutLeavingNpcAtZero() throws Exception {
        var mapper = new ObjectMapper();
        for (var json : new String[]{
                "{\"name\":\"legacy\"}",
                "{\"name\":\"legacy\",\"maxHitpoints\":null,\"maxStamina\":null,\"maxMana\":null,\"dexterity\":null,\"strength\":null,\"intelligence\":null}"
        }) {
            var npc = create(mapper.readValue(json, NpcTemplate.class));
            assertEquals(100, npc.getMaxHitpoints());
            assertEquals(100, npc.getHitpoints());
            assertEquals(100, npc.getMaxStamina());
            assertEquals(100, npc.getStamina());
            assertEquals(100, npc.getMaxMana());
            assertEquals(100, npc.getMana());
            assertEquals(100, npc.getDexterity());
            assertEquals(100, npc.getStrength());
            assertEquals(100, npc.getIntelligence());
        }
    }

    @Test
    void rejectsNonPositiveMaximum() {
        var mapper = new ObjectMapper();
        for (var maximum : new int[]{0, -10}) {
            var error = assertThrows(com.fasterxml.jackson.databind.JsonMappingException.class,
                    () -> mapper.readValue("{\"name\":\"invalid\",\"maxHitpoints\":" + maximum + "}", NpcTemplate.class));
            assertTrue(error.getMessage().contains("NPC maxHitpoints must be positive"));
        }
    }

    @Test
    void createsNpcWithIndependentStaminaAndDexterity() throws Exception {
        var template = new ObjectMapper().readValue(
                "{\"name\":\"custom\",\"maxStamina\":70,\"maxMana\":45,\"dexterity\":35,\"strength\":60,\"intelligence\":20}", NpcTemplate.class);
        var npc = create(template);
        assertEquals(70, npc.getMaxStamina());
        assertEquals(70, npc.getStamina());
        assertEquals(45, npc.getMaxMana());
        assertEquals(45, npc.getMana());
        assertEquals(35, npc.getDexterity());
        assertEquals(60, npc.getStrength());
        assertEquals(20, npc.getIntelligence());
    }

    @Test
    void rejectsNonPositiveStaminaAndDexterity() {
        var mapper = new ObjectMapper();
        for (var field : new String[]{"maxStamina", "maxMana", "dexterity", "strength", "intelligence"}) {
            for (var value : new int[]{0, -10}) {
                var error = assertThrows(com.fasterxml.jackson.databind.JsonMappingException.class,
                        () -> mapper.readValue(
                                "{\"name\":\"invalid\",\"" + field + "\":" + value + "}", NpcTemplate.class));
                assertTrue(error.getMessage().contains("NPC " + field + " must be positive"));
            }
        }
    }

    private UONpc create(NpcTemplate template) {
        var storage = mock(RealmStorage.class);
        when(storage.createMobile(any(UOMobileData.class)))
                .thenAnswer(invocation -> new UONpc(invocation.getArgument(0, UOMobileData.class)));
        var context = new NpcCreationContext(template.name(), mock(Location.class));
        context.setTemplate(template);
        context.setSerialId(42);
        new CreateNpcStep(storage).execute(context);
        return context.getNpc();
    }
}
