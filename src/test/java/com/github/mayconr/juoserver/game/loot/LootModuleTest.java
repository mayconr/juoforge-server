package com.github.mayconr.juoserver.game.loot;

import com.github.mayconr.juoserver.DefaultWorldCfg;
import com.github.mayconr.juoserver.game.item.*;
import com.github.mayconr.juoserver.infrastructure.serialization.JacksonDataMapper;
import com.github.mayconr.juoserver.game.item.flow.creation.*;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerContext;
import com.github.mayconr.juoserver.game.item.flow.open.OpenContainerFlowDefinition;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.loot.flow.generate.*;
import com.github.mayconr.juoserver.game.loot.flow.materialize.*;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.ContainerOpenedEvent;
import com.github.mayconr.juoserver.game.ui.UIModuleImpl;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickContext;
import com.github.mayconr.juoserver.game.ui.flow.doubleclick.DoubleClickFlowDefinition;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.ui.policy.DoubleClickPolicy;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.network.packet.DoubleClick;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LootModuleTest {
    private final JacksonDataMapper dataMapper = new JacksonDataMapper();
    private final PendingLoot pendingLoot = new PendingLoot(dataMapper);
    private final ItemTemplate gold = new ItemTemplate("gold_coin", "gold", null, 0xEED, true, 0, null, 0,
            List.of(ItemFlag.STACKABLE), Map.of(), 0, null, null);

    private LootModuleImpl module(WorldItem items, LootPolicy policy, RNG rng, DefaultFlowRegistry registry) {
        registry.register("loot", GenerateLootFlowDefinition.build(policy, rng, null, null, dataMapper), GenerateLootContext.class);
        registry.register("materialize", MaterializeLootFlowDefinition.build(items, dataMapper), MaterializeLootContext.class);
        var module = new LootModuleImpl();
        module.initialize(DefaultModuleContext.builder().flowFacade(new DefaultFlowFacade(registry)).build());
        return module;
    }

    private UOCorpse corpse() {
        var data = new UOItemData();
        data.setPersistentAttrMap(new DefaultAttributeMap());
        data.setSerialId(0x40000001);
        data.setLocationType(ItemLocationType.GROUND);
        data.setFlags(List.of(ItemFlag.CORPSE));
        return new UOCorpse(data, mock(ItemTemplate.class));
    }

    private LootRequest request(UOCorpse corpse) {
        return new LootRequest(new UONpc(new UOMobileData()), null, DeathCause.COMBAT, corpse);
    }

    @Test
    void defaultConfigurationGeneratesNoLootAndPolicyCanBeReplaced() {
        var cfg = new DefaultWorldCfg();
        assertTrue(cfg.lootPolicy().get().resolve(request(corpse()), mock(RNG.class)).isEmpty());
        LootPolicy policy = (request, random) -> List.of(ItemRequest.byName("gold_coin").withAmount(10));
        cfg.lootPolicy(() -> policy);
        assertSame(policy, cfg.lootPolicy().get());
        assertThrows(NullPointerException.class, () -> cfg.lootPolicy(null));
    }

    @Test
    void storesOnlyTemplatesUntilOpeningAndNeverCreatesAgainOnReopening() {
        var storage = mock(RealmStorage.class);
        var events = mock(EventBus.class);
        var serials = mock(SerialGenerator.class);
        when(serials.getNextItem()).thenReturn(0x40000002);
        when(storage.createItem(any())).thenAnswer(call -> new UOItem(call.getArgument(0), null));
        var registry = new DefaultFlowRegistry();
        registry.register("items", ItemCreationFlowDefinition.build(
                FlowRegistryFactory.GameInfra.builder().storage(storage).eventBus(events).serialGenerator(serials).build(),
                FlowRegistryFactory.GameTemplates.builder().build()), ItemCreationContext.class);
        var items = new ItemModuleImpl(null, null, storage, null, null);
        items.initialize(DefaultModuleContext.builder().flowFacade(new DefaultFlowFacade(registry)).build());
        var rng = mock(RNG.class);
        when(rng.roll(0.5)).thenReturn(true);
        var loot = module(items, (request, random) -> random.roll(0.5)
                ? List.of(ItemRequest.byTemplate(gold).withAmount(10).withHue(7)) : List.of(), rng, registry);
        registry.register("open-container", OpenContainerFlowDefinition.build(storage, events, loot), OpenContainerContext.class);
        var corpse = corpse();
        var request = request(corpse);
        var plan = loot.generate(request);
        assertSame(gold, plan.getFirst().template());
        assertEquals(10, plan.getFirst().amount());
        assertTrue(corpse.getContainerItems().isEmpty());
        verifyNoInteractions(storage, serials, events);
        assertTrue(loot.generate(request).isEmpty());

        var policy = mock(DoubleClickPolicy.class);
        when(policy.allows(any())).thenReturn(true);
        when(storage.getItem(corpse.getSerialId())).thenReturn(Optional.of(corpse));
        var loadedContents = new CompletableFuture<List<UOItem>>();
        when(storage.loadContainerItems(corpse)).thenReturn(loadedContents);
        var handler = new UIModuleImpl(null, null, null);
        registry.register("double-click", DoubleClickFlowDefinition.build(storage, policy, items), DoubleClickContext.class);
        handler.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        var click = mock(DoubleClick.class);
        when(click.getSerialId()).thenReturn(corpse.getSerialId());
        var player = mock(UOPlayer.class);
        doAnswer(call -> {
            assertEquals(1, corpse.getContainerItems().size());
            assertFalse(corpse.persistentAttributes().contains(PendingLoot.TEMPLATES));
            return null;
        }).when(events).publish(new ContainerOpenedEvent(player, corpse));
        handler.doubleClick(player, click);
        assertTrue(corpse.getContainerItems().isEmpty());
        verifyNoInteractions(serials);
        loadedContents.complete(List.of());
        handler.doubleClick(player, click);
        assertEquals(1, corpse.getContainerItems().size());
        assertTrue(corpse.getEquippedItems().isEmpty());
        var captor = org.mockito.ArgumentCaptor.forClass(UOItemData.class);
        verify(storage).createItem(captor.capture());
        assertEquals(10, captor.getValue().getAmount());
        assertEquals(7, captor.getValue().getHue());
        verify(serials, times(1)).getNextItem();
        verify(rng, times(1)).roll(0.5);
        verify(events, times(2)).publish(new ContainerOpenedEvent(player, corpse));
        verifyNoMoreInteractions(events);
    }

    @Test
    void partialMaterializationFailureDoesNotRerollOrRetryOnNextOpening() {
        var items = mock(WorldItem.class);
        var first = ItemRequest.byTemplate(gold);
        var second = first.withAmount(2);
        when(items.createItem(eq(first), any(), any())).thenReturn(mock(UOItem.class));
        when(items.createItem(eq(second), any(), any())).thenThrow(new IllegalStateException("creation failed"));
        var policy = mock(LootPolicy.class);
        var rng = mock(RNG.class);
        var request = request(corpse());
        when(policy.resolve(request, rng)).thenReturn(List.of(first, second));
        var loot = module(items, policy, rng, new DefaultFlowRegistry());
        loot.generate(request);
        verifyNoInteractions(items);
        assertThrows(ModuleOperationException.class, () -> loot.materialize(request.corpse()));
        assertTrue(loot.materialize(request.corpse()).isEmpty());
        assertTrue(loot.generate(request).isEmpty());
        verify(policy, times(1)).resolve(request, rng);
        verify(items, times(2)).createItem(any(), any(), any());
    }

    @Test
    void rejectsLivingNpcsBeforePolicyOrCreation() {
        var items = mock(WorldItem.class);
        var policy = mock(LootPolicy.class);
        var request = request(corpse());
        request.victim().setAlive(true);
        var module = module(items, policy, mock(RNG.class), new DefaultFlowRegistry());
        assertThrows(ModuleOperationException.class, () -> module.generate(request));
        verifyNoInteractions(items, policy);
    }

    private UOCorpse reloadThroughStorageMapping(UOCorpse source) throws Exception {
        var data = source.toData();
        var attributes = new com.github.mayconr.shard.storage.types.AttributesTypeHandler();
        var statement = mock(java.sql.PreparedStatement.class);
        attributes.setNonNullParameter(statement, 1, data.getPersistentAttrMap(), org.apache.ibatis.type.JdbcType.OTHER);
        var json = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(statement).setString(eq(1), json.capture());
        var resultSet = mock(java.sql.ResultSet.class);
        when(resultSet.getString("attr")).thenReturn(json.getValue());
        data.setPersistentAttrMap(attributes.getNullableResult(resultSet, "attr"));
        var templates = new com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry<String, ItemTemplate>(
                List.of(), ItemTemplate::name);
        return assertInstanceOf(UOCorpse.class,
                new com.github.mayconr.juoserver.infrastructure.storage.ItemMapper(templates).mapToItem(data));
    }

    @Test
    void persistedPlanSurvivesReloadAndMaterializesOnceWithoutRerolling() throws Exception {
        var items = mock(WorldItem.class);
        var policy = mock(LootPolicy.class);
        var rng = mock(RNG.class);
        var corpse = corpse();
        var request = request(corpse);
        var drop = ItemRequest.byTemplate(gold).withAmount(10).withHue(7).withDirection(Direction.WEST);
        when(policy.resolve(request, rng)).thenReturn(List.of(drop));
        var loot = module(items, policy, rng, new DefaultFlowRegistry());
        loot.generate(request);
        verifyNoInteractions(items);
        assertTrue(corpse.persistentAttributes().contains(PendingLoot.TEMPLATES));
        assertFalse(corpse.runtimeAttributes().contains(PendingLoot.TEMPLATES));
        assertInstanceOf(Map.class, ((List<?>) corpse.persistentAttributes().get(PendingLoot.TEMPLATES)).getFirst());

        var reloaded = reloadThroughStorageMapping(corpse);
        assertTrue(reloaded.runtimeAttributes().toMap().isEmpty());
        assertEquals(List.of(drop), pendingLoot.read(reloaded));
        assertTrue(loot.generate(request(reloaded)).isEmpty());
        var created = mock(UOItem.class);
        when(items.createItem(eq(drop), any(), any())).thenReturn(created);
        assertEquals(List.of(created), loot.materialize(reloaded));
        assertTrue(loot.materialize(reloaded).isEmpty());
        var reloadedAfterOpening = reloadThroughStorageMapping(reloaded);
        assertTrue(pendingLoot.read(reloadedAfterOpening).isEmpty());
        assertTrue(loot.generate(request(reloadedAfterOpening)).isEmpty());
        assertTrue(loot.materialize(reloadedAfterOpening).isEmpty());
        verify(policy, times(1)).resolve(request, rng);
        verify(items, times(1)).createItem(eq(drop), any(), any());
    }
}
