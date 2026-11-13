package com.github.mayconr.juoserver.infrastructure.storage;

import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOMobileData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SuppressWarnings("deprecation")
class WorldStorageViewsTest {
    private final WorldStorage storage = mock(WorldStorage.class);

    @Test
    void accountsAndSerialsDelegateToTheWorldStorage() {
        var accounts = new WorldStorageViews.AccountsView(storage);
        var serials = new WorldStorageViews.SerialsView(storage);
        var account = mock(UOAccount.class);

        accounts.findByUsername("admin");
        accounts.mobiles(account);
        serials.nextItem();
        serials.nextMobile();

        verify(storage).getAccountByUsername("admin");
        verify(storage).getAccountMobiles(account);
        verify(storage).getNextItemSerial();
        verify(storage).getNextMobileSerial();
    }

    @Test
    void mobilesDelegateToTheWorldStorage() {
        var mobiles = new WorldStorageViews.MobilesView(storage);
        var mobile = mock(UOMobile.class);
        var player = mock(UOPlayer.class);
        var data = mock(UOMobileData.class);
        var from = mock(Location.class);
        var to = mock(Location.class);
        Predicate<UOMobile> filter = candidate -> true;
        List<UOItem> starterItems = List.of();

        mobiles.create(data);
        mobiles.saveNewPlayer(1, player, 2, starterItems);
        mobiles.load(3);
        mobiles.unload(mobile);
        mobiles.get(4);
        mobiles.cache(mobile);
        mobiles.inRange(from, 5, filter);
        mobiles.atLocation(from);
        mobiles.atLocation(from, true);
        mobiles.updateLocation(mobile, from, to);
        mobiles.delete(mobile);
        mobiles.delete(6);
        mobiles.existsByName("name");
        mobiles.save();

        verify(storage).createMobile(data);
        verify(storage).saveNewPlayerMobile(1, player, 2, starterItems);
        verify(storage).loadMobile(3);
        verify(storage).unloadMobile(mobile);
        verify(storage).getMobile(4);
        verify(storage).cache(mobile);
        verify(storage).getMobilesInRange(from, 5, filter);
        verify(storage).getMobilesAtLocation(from);
        verify(storage).getMobilesAtLocation(from, true);
        verify(storage).updateMobileLocation(mobile, from, to);
        verify(storage).deleteMobile(mobile);
        verify(storage).deleteMobile(6);
        verify(storage).mobileExists("name");
        verify(storage).saveMobiles();
    }

    @Test
    void itemsDelegateToTheWorldStorage() {
        var items = new WorldStorageViews.ItemsView(storage, List::of);
        var item = mock(UOItem.class);
        var data = mock(UOItemData.class);
        var container = mock(UOContainer.class);
        var location = mock(Location.class);

        items.create(data);
        items.load(1);
        items.loadContainerItems(container);
        items.get(2);
        items.getContainer(3);
        items.cache(item);
        items.inRange(location);
        items.placeOnGround(item);
        items.removeFromGround(item);
        items.delete(item);
        items.save();

        verify(storage).createItem(data);
        verify(storage).loadItem(1);
        verify(storage).loadContainerItems(container);
        verify(storage).getItem(2);
        verify(storage).getContainer(3);
        verify(storage).cache(item);
        verify(storage).getItemsInRange(location);
        verify(storage).placeOnTheGround(item);
        verify(storage).removeFromTheGround(item);
        verify(storage).deleteItem(item);
        verify(storage).saveItems();
    }

    @Test
    void viewsOfASpiedWorldStorageUseTheSpyOverrides() {
        var worldStorage = spy(new CachedWorldStorage(null, null, null, null));
        var mobile = mock(UOMobile.class);
        doReturn(Optional.of(mobile)).when(worldStorage).getMobile(7);
        doReturn(CompletableFuture.completedFuture(List.of())).when(worldStorage).saveItems();

        assertSame(mobile, worldStorage.mobiles().get(7).orElseThrow());
        worldStorage.items().save().join();

        verify(worldStorage).saveItems();
    }
}
