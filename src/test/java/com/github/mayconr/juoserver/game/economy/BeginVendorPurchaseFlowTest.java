package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.economy.flow.begin.BeginVendorPurchaseContext;
import com.github.mayconr.juoserver.game.economy.flow.begin.BeginVendorPurchaseFlowDefinition;
import com.github.mayconr.juoserver.game.economy.stock.StockEntry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.VendorSessionOpened;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BeginVendorPurchaseFlowTest {
    @Test
    void assignsPricedSessionBeforePublishingOpenedEvent() {
        var serials = mock(SerialGenerator.class);
        var pricing = mock(PricingStrategy.class);
        var events = mock(EventBus.class);
        var player = mock(UOPlayer.class);
        var vendor = mock(UOMobile.class);
        var template = mock(ItemTemplate.class);
        var stock = new StockEntry(template, 10, 1, 5);
        when(serials.getNextVirtual()).thenReturn(123);
        when(pricing.calculate(stock, template)).thenReturn(25);
        var context = new BeginVendorPurchaseContext(player, vendor, mock(RegionNode.class), List.of(stock));

        var result = BeginVendorPurchaseFlowDefinition.build(new VendorHandler(serials, pricing), events)
                .execute(context);

        assertTrue(result.flowSucceeded());
        var session = context.getSession();
        assertEquals(1, session.items().size());
        assertEquals(25, session.items().get(123).price());
        assertSame(stock, session.items().get(123).entry());
        assertEquals(5, stock.getCurrentStock());
        var order = inOrder(player, events);
        order.verify(player).setVendorSession(session);
        order.verify(events).publish(new VendorSessionOpened(player, vendor, session));
    }
}
