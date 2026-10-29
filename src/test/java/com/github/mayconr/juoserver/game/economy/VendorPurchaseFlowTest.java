package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.flow.purchase.*;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.VendorPurchaseCompleted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.network.packet.VendorBuyRequest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VendorPurchaseFlowTest {
    @Test
    void deliversTwoToolsAndConsumesTwoUnitsFromSessionStock() {
        var handler = mock(VendorHandler.class);
        var wallet = mock(com.github.mayconr.juoserver.game.wallet.Wallet.class);
        var items = mock(WorldItem.class);
        var containers = mock(com.github.mayconr.juoserver.game.container.WorldContainer.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var backpack = mock(UOContainer.class);
        var request = mock(VendorBuyRequest.class);
        var template = mock(ItemTemplate.class);
        when(template.flags()).thenReturn(List.of());
        var stock = new StockEntry(template, 10, 1, 5);
        var session = new VendorSession(Map.of(123, new VendorSessionItem(123, 10, stock)));
        when(request.getItems()).thenReturn(List.of(new VendorBuyRequest.BuyItem(123, 2)));
        when(containers.getContainerBySerialId(player.getBackpack())).thenReturn(Optional.of(backpack));
        when(handler.resolveVendorPurchase(player, request, wallet)).thenReturn(
                VendorPurchaseResult.success(List.of(new PurchasedItem(template, 2))));
        var first = mock(UOItem.class);
        var second = mock(UOItem.class);
        var target = ContainerItemTarget.of(backpack, options -> options.tryStack(false));
        when(items.createItem(ItemRequest.byTemplate(template), target)).thenReturn(first, second);

        var result = VendorPurchaseFlowDefinition.build(handler, wallet, items, containers, events)
                .execute(new VendorPurchaseContext(player, request, session));

        assertTrue(result.flowSucceeded());
        assertEquals(3, stock.getCurrentStock());
        assertEquals(2, stock.getConsumedUnits());
        verify(items, times(2)).createItem(ItemRequest.byTemplate(template), target);
        verify(events).publish(new VendorPurchaseCompleted(player, List.of(first, second)));
        verify(player).setVendorSession(null);
    }

    @Test
    void rejectedPurchaseDoesNotDeliverOrConsumeStock() {
        var handler = mock(VendorHandler.class);
        var wallet = mock(com.github.mayconr.juoserver.game.wallet.Wallet.class);
        var items = mock(WorldItem.class);
        var containers = mock(com.github.mayconr.juoserver.game.container.WorldContainer.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var request = mock(VendorBuyRequest.class);
        var stock = new StockEntry(mock(ItemTemplate.class), 10, 1, 5);
        var session = new VendorSession(Map.of(123, new VendorSessionItem(123, 10, stock)));
        when(containers.getContainerBySerialId(player.getBackpack())).thenReturn(Optional.of(mock(UOContainer.class)));
        when(handler.resolveVendorPurchase(player, request, wallet)).thenReturn(VendorPurchaseResult.failed());

        var result = VendorPurchaseFlowDefinition.build(handler, wallet, items, containers, events)
                .execute(new VendorPurchaseContext(player, request, session));

        assertTrue(result.flowFailed());
        assertEquals(5, stock.getCurrentStock());
        verify(items, never()).createItem(any(), any());
        verifyNoInteractions(events);
        verify(player, never()).setVendorSession(any());
    }
}
