package com.github.mayconr.juoserver.network.session;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.MobileDeathEvent;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.network.packet.*;
import com.github.mayconr.juoserver.network.session.handler.CombatEventHandler;
import com.github.mayconr.juoserver.network.session.i18n.ClientLocale;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CorpseLootPacketsTest {
    @Test
    void onlyEquippedItemsEnterCorpseClothingWhileLootRemainsInContainer() {
        var world = mock(WorldInternal.class, RETURNS_DEEP_STUBS);
        var settings = mock(GamePlaySettings.class, RETURNS_DEEP_STUBS);
        when(settings.world().visibility().range()).thenReturn(18);
        var player = mock(UOPlayer.class);
        var npc = new UONpc(new UOMobileData());
        var data = new UOItemData();
        data.setFlags(List.of(ItemFlag.CORPSE));
        data.setLocationType(ItemLocationType.GROUND);
        var corpse = new UOCorpse(data, mock(ItemTemplate.class));
        var weapon = mock(UOItem.class);
        when(weapon.getSerialId()).thenReturn(1);
        when(weapon.getLayer()).thenReturn(Layer.ONE_HANDED);
        var gold = mock(UOItem.class);
        when(gold.getSerialId()).thenReturn(2);
        corpse.addEquippedItem(weapon);
        corpse.addItemToContainer(gold);
        when(world.item().getItemBySerialId(1)).thenReturn(Optional.of(weapon));
        var channel = new EmbeddedChannel();
        try {
            var handler = new CombatEventHandler(new SessionView(() -> player, () -> ClientLocale.PT_BR, settings),
                    new SessionPacketWriter(channel, null), world);
            handler.onMobileDeath(new MobileDeathEvent(player, npc, DeathCause.COMBAT, corpse));
            channel.runPendingTasks();
            var removed = java.util.Set.of(
                    assertInstanceOf(DeleteObject.class, channel.readOutbound()).getSerialId(),
                    assertInstanceOf(DeleteObject.class, channel.readOutbound()).getSerialId());
            assertEquals(java.util.Set.of(1, 2), removed);
            assertInstanceOf(UpdateHealth.class, channel.readOutbound());
            assertInstanceOf(ObjectInfo.class, channel.readOutbound());
            assertInstanceOf(ObjectInfo.class, channel.readOutbound());
            var clothing = assertInstanceOf(CorpseClothing.class, channel.readOutbound());
            var buffer = Unpooled.buffer();
            try {
                clothing.writesTo(buffer);
                assertEquals(13, buffer.readableBytes());
                assertEquals(1, buffer.getInt(8));
            } finally {
                buffer.release();
            }
            assertInstanceOf(DeathAction.class, channel.readOutbound());
            assertNull(channel.readOutbound());
            assertTrue(corpse.getContainerItems().contains(2));
            verify(world.item(), never()).getItemBySerialId(2);
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
