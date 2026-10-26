package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.game.item.*;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.loot.flow.generate.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import java.util.Map;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GenerateLootFlowTest {
    private final WorldItem items = mock(WorldItem.class);
    private final ItemTemplate gold = new ItemTemplate("gold_coin", "gold", null, 0xEED, true, 0, null, 0,
            List.of(ItemFlag.STACKABLE), Map.of(), 0, null, null);
    private final InMemoryTemplateRegistry<String, ItemTemplate> names =
            new InMemoryTemplateRegistry<>(List.of(gold), ItemTemplate::name);
    private final InMemoryTemplateRegistry<Integer, ItemTemplate> models =
            new InMemoryTemplateRegistry<>(List.of(gold), ItemTemplate::modelId);
    private final LootPolicy policy = mock(LootPolicy.class);
    private final RNG rng = mock(RNG.class);

    private LootRequest request() {
        var data = new UOItemData();
        data.setPersistentAttrMap(new DefaultAttributeMap());
        data.setLocationType(ItemLocationType.GROUND);
        return new LootRequest(new UONpc(new UOMobileData()), null, DeathCause.COMBAT,
                new UOCorpse(data, mock(ItemTemplate.class)));
    }

    @Test
    void invalidRequestsFailBeforeReservingOrInvokingPolicy() {
        var flow = GenerateLootFlowDefinition.build(policy, rng, names, models);
        assertEquals("MISSING_LOOT_REQUEST", flow.execute(new GenerateLootContext(null)).code());
        var request = request();
        request.victim().setAlive(true);
        assertEquals("LIVING_LOOT_VICTIM", flow.execute(new GenerateLootContext(request)).code());
        assertTrue(request.corpse().persistentAttributes().toMap().isEmpty());
        request.victim().setAlive(false);
        request.corpse().setCurrentLocation(new EquippedLocation(1));
        assertEquals("INVALID_LOOT_CORPSE", flow.execute(new GenerateLootContext(request)).code());
        verifyNoInteractions(items, policy, rng);
    }

    @Test
    void completesEmptyLootAndStopsDuplicateAttempts() {
        var request = request();
        when(policy.resolve(request, rng)).thenReturn(List.of());
        var flow = GenerateLootFlowDefinition.build(policy, rng, names, models);
        var first = new GenerateLootContext(request);
        assertTrue(flow.execute(first).flowSucceeded());
        assertTrue(first.result().isEmpty());
        var duplicate = new GenerateLootContext(request);
        assertEquals("LOOT_ALREADY_ATTEMPTED", flow.execute(duplicate).code());
        assertTrue(duplicate.result().isEmpty());
        verify(policy, times(1)).resolve(request, rng);
        verifyNoInteractions(items);
    }

    @Test
    void policyFailureKeepsReservationAndOriginalCause() {
        var request = request();
        var cause = new IllegalStateException("policy failed");
        when(policy.resolve(request, rng)).thenThrow(cause);
        var flow = GenerateLootFlowDefinition.build(policy, rng, names, models);
        var result = flow.execute(new GenerateLootContext(request));
        assertTrue(result.flowFailed());
        assertSame(cause, result.cause());
        assertEquals("LOOT_ALREADY_ATTEMPTED", flow.execute(new GenerateLootContext(request)).code());
        verify(policy, times(1)).resolve(request, rng);
        verifyNoInteractions(items);
    }

    @Test
    void storesResolvedTemplatesWithoutCreatingItemsAndReturnsAnImmutablePlan() {
        var request = request();
        var drop = ItemRequest.byName("gold_coin").withAmount(10);
        when(policy.resolve(request, rng)).thenReturn(List.of(drop));
        var context = new GenerateLootContext(request);
        assertTrue(GenerateLootFlowDefinition.build(policy, rng, names, models).execute(context).flowSucceeded());
        assertSame(gold, context.result().getFirst().template());
        assertEquals(10, context.result().getFirst().amount());
        assertTrue(request.corpse().getContainerItems().isEmpty());
        assertEquals(context.result(), PendingLoot.read(request.corpse()));
        assertThrows(UnsupportedOperationException.class, () -> context.result().clear());
        verify(policy).resolve(request, rng);
        verifyNoInteractions(items);
    }

    @Test
    void coreRegistryIncludesGenerationAndDefaultsToEmptyLoot() {
        var infra = FlowRegistryFactory.GameInfra.builder().build();
        assertNotNull(infra.lootPolicy());
        var dependencies = mock(FlowRegistryFactory.GameInfra.class, RETURNS_DEEP_STUBS);
        when(dependencies.lootPolicy()).thenReturn(infra.lootPolicy());
        var registry = FlowRegistryFactory.builder()
                .infra(dependencies)
                .modules(mock(FlowRegistryFactory.GameModules.class, RETURNS_DEEP_STUBS))
                .templates(mock(FlowRegistryFactory.GameTemplates.class, RETURNS_DEEP_STUBS))
                .build().buildRegistry();
        var flow = registry.get(GenerateLootContext.class);
        assertNotNull(flow);
        assertNotNull(registry.get(com.github.mayconr.juoserver.game.loot.flow.materialize.MaterializeLootContext.class));
        var context = new GenerateLootContext(request());
        assertTrue(flow.execute(context).flowSucceeded());
        assertTrue(context.result().isEmpty());
    }
}
