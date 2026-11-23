package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.weight.ContainerWeightContext;
import com.github.mayconr.juoserver.game.container.flow.weight.ContainerWeightFlowDefinition;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContainerWeightFlowTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final ContainerModuleImpl module =
            new ContainerModuleImpl(storage, new ContainerHandler(mock(EventBus.class), storage));

    @BeforeEach
    void initializeModule() {
        when(storage.items()).thenReturn(items);
        var registry = new DefaultFlowRegistry();
        registry.register("container-weight",
                ContainerWeightFlowDefinition.build(storage), ContainerWeightContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
    }

    @Test
    void sumsContainerItselfAndNestedContents() {
        var root = container(1, 3, List.of(2, 3));
        item(2, 1, 10);
        container(3, 2, List.of(4));
        item(4, 4, 5);

        assertEquals(3 + 10 + 2 + 20, module.getContentsWeight(root));
    }

    @Test
    void emptyContainerWeighsOnlyItself() {
        assertEquals(5, module.getContentsWeight(container(1, 5, List.of())));
    }

    @Test
    void rejectsMissingContainer() {
        assertThrows(IllegalArgumentException.class, () -> module.getContentsWeight(null));
    }

    @Test
    void failsOnUnloadedItemOrCycle() {
        var root = container(1, 1, List.of(2));
        assertThrows(IllegalStateException.class, () -> module.getContentsWeight(root));

        container(2, 1, List.of(1));
        assertThrows(IllegalStateException.class, () -> module.getContentsWeight(root));
    }

    private UOContainer container(int serial, int unitWeight, List<Integer> contents) {
        var container = mock(UOContainer.class);
        when(container.getSerialId()).thenReturn(serial);
        when(container.getUnitWeight()).thenReturn(unitWeight);
        when(container.getAmount()).thenReturn(1);
        when(container.getContainerItems()).thenReturn(contents);
        when(items.get(serial)).thenReturn(Optional.of(container));
        return container;
    }

    private UOItem item(int serial, int unitWeight, int amount) {
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(serial);
        when(item.getUnitWeight()).thenReturn(unitWeight);
        when(item.getAmount()).thenReturn(amount);
        when(items.get(serial)).thenReturn(Optional.of(item));
        return item;
    }
}
