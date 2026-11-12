package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemContext;
import com.github.mayconr.juoserver.game.container.flow.add.PlaceContainerItemStep;
import com.github.mayconr.juoserver.game.model.ContainerLocation;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.OrphanLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlaceContainerItemStepTest {
    private static final int CONTAINER = 0x40000099;
    private final Items items = mock(Items.class);
    private final WorldStorage storage = mock(WorldStorage.class);
    private final PlaceContainerItemStep step = new PlaceContainerItemStep(storage);

    PlaceContainerItemStepTest() {
        when(storage.items()).thenReturn(items);
    }

    @Test
    void orphanContainerRejectsTheItem() {
        var container = container(new OrphanLocation());
        var item = item(0x40000001, "iron_ore", true);

        var result = step.execute(new AddContainerItemContext(container, item, true, false));

        assertTrue(result.flowFailed());
        assertEquals("ITEM_CONTAINER_LOCATION_UNSUPPORTED", result.code());
        assertTrue(container.getContainerItems().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ground", "equipped", "container"})
    void supportedContainersReceiveANewItem(String where) {
        var container = container(location(where));
        var item = item(0x40000001, "sword", false);
        var context = new AddContainerItemContext(container, item, true, false);

        assertTrue(step.execute(context).flowSucceeded());

        assertPlaced(context, container, item);
    }

    @Test
    void equippedContainerJoinsAnExistingStackOfTheSameItem() {
        var container = container(new EquippedLocation(1));
        var existing = stored(container, item(0x40000001, "iron_ore", true));
        var added = item(0x40000002, "iron_ore", true);
        var context = new AddContainerItemContext(container, added, true, false);

        assertTrue(step.execute(context).flowSucceeded());

        assertSame(existing, context.result());
        assertEquals(2, existing.getAmount());
        assertEquals(List.of(existing.getSerialId()), List.copyOf(container.getContainerItems()));
    }

    @Test
    void noStackIsJoinedWhenStackingIsNotRequested() {
        var container = container(new EquippedLocation(1));
        stored(container, item(0x40000001, "iron_ore", true));
        var added = item(0x40000002, "iron_ore", true);
        var context = new AddContainerItemContext(container, added, false, false);

        step.execute(context);

        assertPlaced(context, container, added);
    }

    @Test
    void nonStackableItemsAreNeverJoined() {
        var container = container(new EquippedLocation(1));
        stored(container, item(0x40000001, "sword", false));
        var added = item(0x40000002, "sword", false);
        var context = new AddContainerItemContext(container, added, true, false);

        step.execute(context);

        assertPlaced(context, container, added);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ground", "container"})
    void onlyEquippedContainersJoinStacks(String where) {
        var container = container(location(where));
        stored(container, item(0x40000001, "iron_ore", true));
        var added = item(0x40000002, "iron_ore", true);
        var context = new AddContainerItemContext(container, added, true, false);

        step.execute(context);

        assertPlaced(context, container, added);
    }

    @Test
    void itemsWithAnotherNameAreNotJoined() {
        var container = container(new EquippedLocation(1));
        stored(container, item(0x40000001, "copper_ore", true));
        var added = item(0x40000002, "iron_ore", true);
        var context = new AddContainerItemContext(container, added, true, false);

        step.execute(context);

        assertPlaced(context, container, added);
    }

    @Test
    void anItemAlreadyInTheContainerDoesNotJoinItself() {
        var container = container(new EquippedLocation(1));
        var item = stored(container, item(0x40000001, "iron_ore", true));
        var context = new AddContainerItemContext(container, item, true, false);

        step.execute(context);

        assertSame(item, context.result());
        assertEquals(1, item.getAmount());
    }

    @Test
    void missingContainerItemFailsWhileLookingForAStack() {
        var container = container(new EquippedLocation(1));
        container.addItemToContainer(item(0x40000001, "iron_ore", true));
        when(items.get(0x40000001)).thenReturn(Optional.empty());
        var context = new AddContainerItemContext(container, item(0x40000002, "iron_ore", true), true, false);

        assertThrows(IllegalStateException.class, () -> step.execute(context));
    }

    private void assertPlaced(AddContainerItemContext context, UOContainer container, UOItem item) {
        assertSame(item, context.result());
        assertEquals(ItemLocation.container(CONTAINER), item.getCurrentLocation());
        assertTrue(container.getContainerItems().contains(item.getSerialId()));
        assertEquals(1, item.getAmount());
    }

    private UOItem stored(UOContainer container, UOItem item) {
        item.setCurrentLocation(ItemLocation.container(CONTAINER));
        container.addItemToContainer(item);
        when(items.get(item.getSerialId())).thenReturn(Optional.of(item));
        return item;
    }

    private static ItemLocation location(String where) {
        return switch (where) {
            case "ground" -> new GroundLocation();
            case "equipped" -> new EquippedLocation(1);
            case "container" -> new ContainerLocation(0x40000500);
            default -> throw new IllegalArgumentException(where);
        };
    }

    private static UOContainer container(ItemLocation location) {
        var data = data(CONTAINER, "backpack");
        data.setFlags(List.of(ItemFlag.CONTAINER));
        var container = new UOContainer(data, null);
        container.setCurrentLocation(location);
        return container;
    }

    private static UOItem item(int serial, String name, boolean stackable) {
        var data = data(serial, name);
        data.setFlags(stackable ? List.of(ItemFlag.STACKABLE) : List.of());
        return new UOItem(data, null);
    }

    private static UOItemData data(int serial, String name) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setName(name);
        data.setAmount(1);
        data.setLocationType(ItemLocationType.ORPHAN);
        return data;
    }
}
