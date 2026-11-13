package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.ItemCreationResult.Created;
import com.github.mayconr.juoserver.game.item.ItemCreationResult.Rejected;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.equip.EquipHeldItemContext;
import com.github.mayconr.juoserver.game.item.flow.lift.LiftItemContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.network.packet.DropItem;
import com.github.mayconr.juoserver.network.packet.EquipItemRequest;
import com.github.mayconr.juoserver.network.packet.UnequipItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemModuleTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final FlowFacade flows = mock(FlowFacade.class);
    private final ItemUseService itemUse = mock(ItemUseService.class);
    private final ItemModule module = new ItemModuleImpl(storage, mock(ItemTemplateRegistry.class), itemUse);

    @BeforeEach
    void setUp() {
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(flows);
        module.initialize(context);
    }

    @Test
    void dispatchesItemUseToTheService() {
        var context = new ItemUseContext(mock(UOPlayer.class), mock(UOItem.class), Trigger.DOUBLE_CLICK);
        module.use(context);
        verify(itemUse).use(context);
    }

    @Test
    void deletesResolvedItemThroughTheDeletionFlow() {
        int serial = 0x40000001;
        var item = mock(UOItem.class);
        when(storage.items()).thenReturn(items);
        when(items.get(serial)).thenReturn(Optional.of(item));
        when(flows.execute(any(DeleteItemContext.class))).thenReturn(StepResult.success());

        module.deleteItem(serial);

        var context = ArgumentCaptor.forClass(DeleteItemContext.class);
        verify(flows).execute(context.capture());
        assertSame(item, context.getValue().getItem());
    }

    @Test
    void failedDeletionFlowRaisesModuleError() {
        when(flows.execute(any(DeleteItemContext.class))).thenReturn(StepResult.failure("FAILED", "failed"));

        assertThrows(ModuleOperationException.class, () -> module.deleteItem(mock(UOItem.class)));
    }

    @Test
    void tryCreateItemReturnsTheCreatedItem() {
        var item = mock(UOItem.class);
        when(flows.execute(any(ItemCreationContext.class))).thenAnswer(invocation -> {
            invocation.<ItemCreationContext>getArgument(0).complete(item);
            return StepResult.success();
        });

        var result = module.tryCreateItem(ItemRequest.byName("gold"), ItemTarget.orphan());

        assertEquals(new Created(item), result);
        assertSame(item, result.orThrow());
    }

    @Test
    void tryCreateItemReportsGameRuleRejection() {
        when(flows.execute(any(ItemCreationContext.class)))
                .thenReturn(StepResult.failure("BACKPACK_OVERWEIGHT", "Estimated weight 81 exceeds 80"));

        var result = module.tryCreateItem(ItemRequest.byName("gold"), ItemTarget.orphan());

        var rejected = assertInstanceOf(Rejected.class, result);
        assertEquals(ItemCreationFailure.BACKPACK_OVERWEIGHT, rejected.reason());
        assertEquals("Estimated weight 81 exceeds 80", rejected.detail());
        assertEquals("{item.backpack_overweight}", rejected.displayMessage());
    }

    @Test
    void tryCreateItemRaisesFailuresThatAreNotGameRuleRejections() {
        when(flows.execute(any(ItemCreationContext.class)))
                .thenReturn(StepResult.failure("ITEM_TARGET_MISMATCH", "wrong target"));

        var error = assertThrows(ModuleOperationException.class,
                () -> module.tryCreateItem(ItemRequest.byName("gold"), ItemTarget.orphan()));

        assertEquals("ITEM_TARGET_MISMATCH", error.getCode());
        assertEquals("{item.creation_failed}", error.getDisplayMessage());
    }

    @Test
    void throwingResultRaisesGameRuleRejection() {
        when(flows.execute(any(ItemCreationContext.class)))
                .thenReturn(StepResult.failure("ITEM_NOT_STACKABLE", "not stackable", "{item.not_stackable}"));

        var error = assertThrows(ModuleOperationException.class,
                () -> module.tryCreateItem(ItemRequest.byName("gold").withAmount(2), ItemTarget.orphan()).orThrow());

        assertEquals("createItem", error.getOperation());
        assertEquals("ITEM_NOT_STACKABLE", error.getCode());
        assertEquals("not stackable", error.getMessage());
        assertEquals("{item.not_stackable}", error.getDisplayMessage());
    }

    @Test
    void rejectsMobileSerialBeforeAccessingStorage() {
        clearInvocations(storage);
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(1));
        verifyNoInteractions(storage, flows);
    }

    @Test
    void dropEndsTheHoldEvenWhenTheFlowFails() {
        var player = holdingPlayer();
        when(flows.execute(any(DropItemContext.class))).thenThrow(new IllegalStateException("failed"));

        assertThrows(IllegalStateException.class, () -> module.dropItem(player, mock(DropItem.class)));

        assertFalse(module.heldItems().isHoldingAny(player));
    }

    @Test
    void equipEndsTheHold() {
        var player = holdingPlayer();
        when(flows.execute(any(EquipHeldItemContext.class))).thenReturn(StepResult.failure("ITEM_NOT_HELD", "not held"));

        module.equipHeldItem(player, mock(EquipItemRequest.class));

        assertFalse(module.heldItems().isHoldingAny(player));
    }

    @Test
    void releaseEndsTheHold() {
        var player = holdingPlayer();

        module.releaseHeldItem(player);

        assertFalse(module.heldItems().isHoldingAny(player));
    }

    @Test
    void liftRunsTheLiftFlow() {
        var player = mock(UOPlayer.class);
        var request = mock(UnequipItem.class);

        module.liftItem(player, request);

        var context = ArgumentCaptor.forClass(LiftItemContext.class);
        verify(flows).execute(context.capture());
        assertSame(player, context.getValue().getPlayer());
        assertSame(request, context.getValue().getRequest());
    }

    private UOPlayer holdingPlayer() {
        var player = mock(UOPlayer.class);
        when(player.getSerialId()).thenReturn(1);
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(0x40000001);
        assertTrue(module.heldItems().hold(player, item));
        return player;
    }

    @Test
    void missingItemDoesNotRunTheDeletionFlow() {
        int serial = 0x40000001;
        when(storage.items()).thenReturn(items);
        when(items.get(serial)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(serial));
        verifyNoInteractions(flows);
    }
}
