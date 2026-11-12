package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.container.flow.add.AddContainerItemContext;
import com.github.mayconr.juoserver.game.container.flow.add.PlaceContainerItemStep;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext;
import com.github.mayconr.juoserver.game.item.flow.drop.DropItemContext.TargetStack;
import com.github.mayconr.juoserver.game.item.flow.drop.placement.StackItemStep;
import com.github.mayconr.juoserver.game.model.EquippedLocation;
import com.github.mayconr.juoserver.game.model.GroundLocation;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.ItemMaterial;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Stacks merged by any path keep the material averaged by the amounts of both stacks. */
class StackMaterialTest {
    private static final ItemMaterial IRON = new ItemMaterial(70, 60, 65, 10);
    private static final ItemMaterial BRONZE = new ItemMaterial(80, 70, 60, 5);

    @Nested
    class AbsorbStack {

        @Test
        void averagesTheMaterialUsingTheAmountsBeforeTheMerge() {
            var stack = item(0x40000001, 30, IRON);
            var other = item(0x40000002, 10, BRONZE);

            stack.absorbStack(other);

            assertEquals(40, stack.getAmount());
            assertEquals(new ItemMaterial(73, 63, 64, 9), stack.getMaterial());
        }

        @Test
        void leavesTheAbsorbedStackUnchanged() {
            var stack = item(0x40000001, 30, IRON);
            var other = item(0x40000002, 10, BRONZE);

            stack.absorbStack(other);

            assertEquals(10, other.getAmount());
            assertEquals(BRONZE, other.getMaterial());
        }

        @Test
        void mergedMaterialIsPersistedWithTheStack() {
            var stack = item(0x40000001, 1, IRON);

            stack.absorbStack(item(0x40000002, 1, BRONZE));

            assertEquals(new ItemMaterial(75, 65, 63, 8), stack.toData().getMaterial());
        }

        @Test
        void stacksWithoutMaterialOnlySumTheAmounts() {
            var stack = item(0x40000001, 2, null);

            stack.absorbStack(item(0x40000002, 3, null));

            assertEquals(5, stack.getAmount());
            assertNull(stack.getMaterial());
        }

        @Test
        void repeatedMergesMatchASingleWeightedAverage() {
            var stack = item(0x40000001, 10, new ItemMaterial(100, 100, 100, 100));

            stack.absorbStack(item(0x40000002, 10, new ItemMaterial(0, 0, 0, 0)));
            stack.absorbStack(item(0x40000003, 20, new ItemMaterial(50, 50, 50, 50)));

            assertEquals(new ItemMaterial(50, 50, 50, 50), stack.getMaterial());
            assertEquals(40, stack.getAmount());
        }
    }

    @Nested
    class DropOntoStack {
        private final Items items = mock(Items.class);
        private final WorldStorage storage = storage(items);
        private final StackItemStep step = new StackItemStep(storage, mock(EventBus.class));

        @Test
        void droppedStackMaterialIsAveragedIntoTheTargetStack() {
            var target = item(0x40000001, 30, IRON);
            target.setCurrentLocation(new GroundLocation());
            var dropped = item(0x40000002, 10, BRONZE);
            var context = DropItemContext.ofStack(mock(UOPlayer.class), new TargetStack(target));
            context.setItem(dropped);

            assertTrue(step.execute(context).flowSucceeded());

            assertEquals(40, target.getAmount());
            assertEquals(new ItemMaterial(73, 63, 64, 9), target.getMaterial());
            verify(items).delete(dropped);
        }
    }

    @Nested
    class AddToContainer {
        private final Items items = mock(Items.class);
        private final WorldStorage storage = storage(items);
        private final PlaceContainerItemStep step = new PlaceContainerItemStep(storage);

        @Test
        void stackableItemAddedToAnExistingStackAveragesItsMaterial() {
            var backpack = backpack();
            var existing = stackable(0x40000001, 30, IRON);
            backpack.addItemToContainer(existing);
            when(items.get(existing.getSerialId())).thenReturn(Optional.of(existing));
            var added = stackable(0x40000002, 10, BRONZE);
            var context = new AddContainerItemContext(backpack, added, true, false);

            assertTrue(step.execute(context).flowSucceeded());

            assertSame(existing, context.result());
            assertEquals(40, existing.getAmount());
            assertEquals(new ItemMaterial(73, 63, 64, 9), existing.getMaterial());
        }

        private UOContainer backpack() {
            var data = data(0x40000099, 1, null);
            data.setFlags(List.of(ItemFlag.CONTAINER));
            var backpack = new UOContainer(data, null);
            backpack.setCurrentLocation(new EquippedLocation(1));
            return backpack;
        }

        private UOItem stackable(int serial, int amount, ItemMaterial material) {
            var data = data(serial, amount, material);
            data.setFlags(List.of(ItemFlag.STACKABLE));
            return new UOItem(data, null);
        }
    }

    private static WorldStorage storage(Items items) {
        var storage = mock(WorldStorage.class);
        when(storage.items()).thenReturn(items);
        return storage;
    }

    private static UOItem item(int serial, int amount, ItemMaterial material) {
        return new UOItem(data(serial, amount, material), null);
    }

    private static UOItemData data(int serial, int amount, ItemMaterial material) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setName("iron_ore");
        data.setAmount(amount);
        data.setMaterial(material);
        data.setFlags(List.of());
        data.setLocationType(ItemLocationType.ORPHAN);
        return data;
    }
}
