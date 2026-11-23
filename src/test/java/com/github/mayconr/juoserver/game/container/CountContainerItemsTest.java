package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.count.CountContainerItemsContext;
import com.github.mayconr.juoserver.game.container.flow.count.CountContainerItemsFlowDefinition;
import com.github.mayconr.juoserver.game.item.ContainerHandler;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CountContainerItemsTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final ContainerModuleImpl module =
            new ContainerModuleImpl(storage, new ContainerHandler(mock(EventBus.class), storage));

    @BeforeEach
    void initializeModule() {
        when(storage.items()).thenReturn(items);
        var registry = new DefaultFlowRegistry();
        registry.register("count-container-items",
                CountContainerItemsFlowDefinition.build(storage), CountContainerItemsContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
    }

    @Test
    void rejectsMissingArgumentsBeforeQueryingStorage() {
        var root = container(1, "backpack", List.of());
        var options = new CountItemsOptions(true, true, false);

        assertThrows(IllegalArgumentException.class, () -> module.countItems(null, "arrow", options));
        assertThrows(IllegalArgumentException.class, () -> module.countItems(root, null, options));
        assertThrows(IllegalArgumentException.class, () -> module.countItems(root, "arrow", null));
        verify(root, never()).getContainerItems();
    }

    @Test
    void countsDirectChildrenOnlyByDefault() {
        var root = container(1, "backpack", List.of(2, 3));
        item(2, "arrow", 10);
        container(3, "bag", List.of(4));
        item(4, "arrow", 5);

        assertEquals(10, module.countItems(root, "arrow", true, false));
    }

    @Test
    void countsNestedContainersWhenRequested() {
        var root = container(1, "backpack", List.of(2, 3));
        item(2, "arrow", 10);
        container(3, "bag", List.of(4));
        item(4, "arrow", 5);

        assertEquals(15, module.countItems(root, "arrow", true, true));
    }

    @Test
    void countsEachMatchOnceWhenStackAmountIsIgnored() {
        var root = container(1, "backpack", List.of(2, 3, 5));
        item(2, "arrow", 10);
        container(3, "bag", List.of(4));
        item(4, "arrow", 5);
        item(5, "bolt", 7);

        assertEquals(2, module.countItems(root, "arrow", false, true));
    }

    @Test
    void countsMatchingNestedContainersThemselves() {
        var root = container(1, "backpack", List.of(2));
        container(2, "bag", List.of(3));
        container(3, "bag", List.of());

        assertEquals(2, module.countItems(root, "bag", false, true));
    }

    @Test
    void failsOnUnloadedItemOrCycle() {
        var root = container(1, "backpack", List.of(2));
        assertThrows(IllegalStateException.class, () -> module.countItems(root, "arrow", true, false));

        container(2, "bag", List.of(1));
        when(items.get(1)).thenReturn(Optional.of(root));
        assertThrows(IllegalStateException.class, () -> module.countItems(root, "arrow", true, true));
    }

    @Test
    void defaultOverloadCountsStacksWithoutNestedContainers() {
        var containers = mock(WorldContainer.class, CALLS_REAL_METHODS);
        var root = mock(UOContainer.class);
        var options = new CountItemsOptions(true, false, false);
        when(containers.countItems(root, "arrow", options)).thenReturn(3);

        assertEquals(3, containers.countItems(root, "arrow"));
        verify(containers).countItems(root, "arrow", options);
    }

    @Test
    void warmupCountsPersistedAndCachedStacksWithoutDuplicates() {
        var root = container(1, "backpack", List.of(3, 5));
        var bag = container(2, "bag", List.of());
        var direct = item(3, "arrow", 10);
        var nested = item(4, "arrow", 5);
        item(5, "arrow", 2);
        when(items.loadContainerItems(root)).thenReturn(CompletableFuture.completedFuture(List.of(direct, bag)));
        when(items.loadContainerItems(bag)).thenReturn(CompletableFuture.completedFuture(List.of(nested)));

        assertEquals(17, module.countItems(root, "arrow", new CountItemsOptions(true, true, true)));
    }

    private UOContainer container(int serial, String name, List<Integer> contents) {
        var container = mock(UOContainer.class);
        when(container.getSerialId()).thenReturn(serial);
        when(container.getName()).thenReturn(name);
        when(container.getAmount()).thenReturn(1);
        when(container.getContainerItems()).thenReturn(contents);
        when(items.get(serial)).thenReturn(Optional.of(container));
        return container;
    }

    private UOItem item(int serial, String name, int amount) {
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(serial);
        when(item.getName()).thenReturn(name);
        when(item.getAmount()).thenReturn(amount);
        when(items.get(serial)).thenReturn(Optional.of(item));
        return item;
    }
}
