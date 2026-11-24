package com.github.mayconr.juoserver.game.economy;

import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.game.mobile.flow.gold.RecalculateGoldContext;
import com.github.mayconr.juoserver.game.mobile.flow.gold.RecalculateGoldFlowDefinition;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.game.model.event.ItemInventoryChanged;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.game.model.event.MobileGoldChanged;
import com.github.mayconr.juoserver.game.ui.WorldUI;
import com.github.mayconr.juoserver.game.wallet.BackpackGoldUpdates;
import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import com.github.mayconr.juoserver.infrastructure.storage.mobile.Mobiles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BackpackGoldUpdatesTest {
    private final Wallet wallet = mock(Wallet.class);
    private final MobileModule mobiles = mock(MobileModule.class);
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final Mobiles mobileStorage = mock(Mobiles.class);
    private final EventBus events = new DefaultEventBus();
    private final UOItem coin = mock(UOItem.class);

    @BeforeEach
    void registerGoldUpdates() {
        when(storage.items()).thenReturn(items);
        when(storage.mobiles()).thenReturn(mobileStorage);
        when(wallet.isGold(coin)).thenReturn(true);
        new BackpackGoldUpdates(wallet, mobiles, storage).register(events);
    }

    @Test
    void transferringCoinsRefreshesBothBackpackOwners() {
        int source = 0x40000001;
        int destination = 0x40000002;
        var sender = backpackOwner(1, source);
        var recipient = backpackOwner(2, destination);

        events.publish(new ItemInventoryChanged(coin, ItemLocation.container(source),
                ItemLocation.container(destination)));

        verify(mobiles).recalculateGold(sender);
        verify(mobiles).recalculateGold(recipient);
    }

    @Test
    void combiningCoinsInOneBackpackRefreshesItsOwnerOnce() {
        int backpack = 0x40000001;
        var player = backpackOwner(1, backpack);
        var location = ItemLocation.container(backpack);

        events.publish(new ItemInventoryChanged(coin, location, location));

        verify(mobiles).recalculateGold(player);
    }

    @Test
    void changingOrDeletingACoinRefreshesItsBackpackOwner() {
        int backpack = 0x40000001;
        var player = backpackOwner(1, backpack);
        when(coin.getCurrentLocation()).thenReturn(ItemLocation.container(backpack));

        events.publish(new ItemUpdated(coin, null));
        verify(mobiles).recalculateGold(player);

        events.publish(new ItemDeleted(coin));
        verify(mobiles, times(2)).recalculateGold(player);
    }

    @Test
    void sameBalanceSkipsGoldEventAndStatusPacket() {
        var notifications = mock(EventBus.class);
        var ui = mock(WorldUI.class);
        var player = mock(UOPlayer.class);
        var balance = new AtomicInteger(10);
        when(player.getGold()).thenAnswer(call -> balance.get());
        when(wallet.getBalance(player)).thenReturn(10, 15);
        var flow = RecalculateGoldFlowDefinition.build(wallet, notifications, ui);

        assertTrue(flow.execute(new RecalculateGoldContext(player, balance::set)).flowSucceeded());
        verifyNoInteractions(notifications, ui);

        assertTrue(flow.execute(new RecalculateGoldContext(player, balance::set)).flowSucceeded());
        assertEquals(15, balance.get());
        verify(notifications).publish(new MobileGoldChanged(player, 10, 15));
        verify(ui).sendStatusGump(player, player.getSerialId());
    }

    private UOPlayer backpackOwner(int mobileSerial, int backpackSerial) {
        var player = mock(UOPlayer.class);
        var backpack = mock(UOContainer.class);
        when(player.getBackpack()).thenReturn(backpackSerial);
        when(backpack.getCurrentLocation()).thenReturn(ItemLocation.equipped(mobileSerial));
        when(items.getContainer(backpackSerial)).thenReturn(Optional.of(backpack));
        when(mobileStorage.get(mobileSerial)).thenReturn(Optional.of(player));
        return player;
    }
}
