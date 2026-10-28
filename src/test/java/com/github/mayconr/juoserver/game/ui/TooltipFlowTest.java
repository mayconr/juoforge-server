package com.github.mayconr.juoserver.game.ui;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.TooltipRequested;
import com.github.mayconr.juoserver.game.ui.flow.tooltip.TooltipContext;
import com.github.mayconr.juoserver.game.ui.flow.tooltip.TooltipFlowDefinition;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

class TooltipFlowTest {
    private final RealmStorage storage = mock(RealmStorage.class);
    private final EventBus events = mock(EventBus.class);
    private final UOPlayer player = mock(UOPlayer.class);

    private WorldUI ui() {
        var registry = new DefaultFlowRegistry();
        registry.register("tooltip", TooltipFlowDefinition.build(events, storage), TooltipContext.class);
        var module = new UIModuleImpl(null);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
        return module;
    }

    @Test
    void collectsMobileItemAndVendorEntryInRequestOrder() {
        var mobile = mock(UONpc.class);
        var item = mock(UOItem.class);
        var vendorItem = mock(VendorSessionItem.class);
        when(storage.getMobile(42)).thenReturn(Optional.of(mobile));
        when(storage.getItem(0x40000001)).thenReturn(Optional.of(item));
        when(player.getVendorSession()).thenReturn(new VendorSession(Map.of(0x60000001, vendorItem)));

        ui().tooltipRequest(player, List.of(42, 0x40000001, 0x60000001));

        verify(events).publish(new TooltipRequested(player, List.of(mobile, item, vendorItem)));
    }

    @Test
    void skipsMissingObjectsAndVirtualSerialWithoutVendorSession() {
        when(storage.getMobile(42)).thenReturn(Optional.empty());
        when(storage.getItem(0x40000001)).thenReturn(Optional.empty());

        ui().tooltipRequest(player, List.of(42, 0x40000001, 0x60000001, 0));

        verifyNoInteractions(events);
    }

    @Test
    void emptyRequestDoesNotAccessStorageOrPublishEvent() {
        ui().tooltipRequest(player, List.of());

        verifyNoInteractions(storage, events);
    }
}