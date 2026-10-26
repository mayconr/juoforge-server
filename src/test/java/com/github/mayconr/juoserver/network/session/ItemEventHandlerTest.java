package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.network.session.handler.ItemEventHandler;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.*;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemEventHandlerTest {
    @Test
    void stackContentsArePrivateButRemovalStillReachesObservers() {
        var channel = new EmbeddedChannel();
        try {
            var player = mock(UOPlayer.class);
            when(player.getSerialId()).thenReturn(1);
            var container = mock(UOContainer.class);
            when(container.getCurrentLocation()).thenReturn(new EquippedLocation(2));
            var handler = new ItemEventHandler(new SessionView(() -> player, () -> ClientLocale.PT_BR, null),
                    new SessionPacketWriter(channel, null), null);
            handler.onItemStacked(new ItemStacked(player, mock(UOItem.class), mock(UOItem.class),
                    ItemStacked.StackDestination.CONTAINER, container));
            assertInstanceOf(DeleteObject.class, channel.readOutbound());
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void equipmentRemovalUpdatesNearbyObserversWithoutBroadcasting() {
        var channel = new EmbeddedChannel();
        try {
            var player = mock(UOPlayer.class);
            var other = mock(UOPlayer.class);
            when(other.isConnected()).thenReturn(true);
            var settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);
            when(settings.world().visibility().range()).thenReturn(18);
            var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
            when(world.mobile().getEquippedItems(other)).thenReturn(Map.of());
            var handler = new ItemEventHandler(new SessionView(() -> player, () -> ClientLocale.PT_BR, settings),
                    new SessionPacketWriter(channel, null), world);
            handler.onItemUnequipped(new ItemUnequipped(other, mock(UOItem.class)));
            assertInstanceOf(DrawMobile.class, channel.readOutbound());
            assertNull(channel.readOutbound());
            when(other.getX()).thenReturn(1000);
            handler.onItemUnequipped(new ItemUnequipped(other, mock(UOItem.class)));
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
