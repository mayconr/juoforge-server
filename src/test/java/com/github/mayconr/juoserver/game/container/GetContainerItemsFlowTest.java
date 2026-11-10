package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.query.GetContainerItemsContext;
import com.github.mayconr.juoserver.game.container.flow.query.GetContainerItemsFlowDefinition;
import com.github.mayconr.juoserver.game.item.ContainerHandler;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetContainerItemsFlowTest {
    private final RealmStorage storage = mock(RealmStorage.class);
    private final ContainerModuleImpl module = new ContainerModuleImpl(storage, new ContainerHandler(mock(EventBus.class), storage));

    @BeforeEach
    void initializeModule() {
        var registry = new DefaultFlowRegistry();
        registry.register("get-container-items", GetContainerItemsFlowDefinition.build(storage), GetContainerItemsContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
    }

    @Test
    void nonrecursiveQueryReturnsOnlyDirectChildren() {
        var bag = container(2, List.of(3));
        var item = item(3);
        container(1, List.of(2));

        assertEquals(List.of(bag), module.getItemsInContainer(1, candidate -> true, false));
        verify(storage, never()).getItem(item.getSerialId());
    }

    @Test
    void recursiveQueryTraversesContainersExcludedByPredicate() {
        container(1, List.of(2));
        container(2, List.of(3));
        container(3, List.of(4));
        var target = item(4);

        var result = module.getItemsInContainer(1, candidate -> candidate == target, true);

        assertEquals(List.of(target), result);
    }

    @Test
    void recursiveQuerySkipsCyclesAndRepeatedItems() {
        container(1, List.of(2, 3));
        var bag = container(2, List.of(1, 2, 3));
        var target = item(3);

        assertEquals(List.of(bag, target), module.getItemsInContainer(1, candidate -> true, true));
        verify(storage).getItem(3);
        verify(storage, never()).getItem(1);
    }

    @Test
    void missingRootOrReferencedItemFails() {
        assertThrows(IllegalArgumentException.class, () -> module.getItemsInContainer(1, item -> true, true));
        container(1, List.of(2));
        assertThrows(IllegalArgumentException.class, () -> module.getItemsInContainer(1, item -> true, false));
    }

    @Test
    void existingOverloadDefaultsToNonrecursiveQuery() {
        var containers = mock(WorldContainer.class, CALLS_REAL_METHODS);
        var target = mock(UOItem.class);
        when(containers.getItemsInContainer(eq(1), any(), eq(false))).thenReturn(List.of(target));

        assertEquals(List.of(target), containers.getItemsInContainer(1, item -> true));
        verify(containers).getItemsInContainer(eq(1), any(), eq(false));
    }

    @Test
    void rejectsInvalidArgumentsBeforeQueryingStorage() {
        assertThrows(IllegalArgumentException.class, () -> module.getItemsInContainer(null, item -> true, true));
        assertThrows(IllegalArgumentException.class, () -> module.getItemsInContainer(1, null, true));
        verify(storage, never()).getContainer(anyInt());
    }

    @Test
    void emptyContainerReturnsAnEmptyList() {
        container(1, List.of());

        assertTrue(module.getItemsInContainer(1, item -> true, true).isEmpty());
    }

    @Test
    void registersQueryInCoreFlowRegistry() {
        var infra = mock(FlowRegistryFactory.GameInfra.class, RETURNS_DEEP_STUBS);
        when(infra.storage()).thenReturn(storage);
        var registry = FlowRegistryFactory.builder()
                .infra(infra)
                .modules(mock(FlowRegistryFactory.GameModules.class, RETURNS_DEEP_STUBS))
                .templates(mock(FlowRegistryFactory.GameTemplates.class, RETURNS_DEEP_STUBS))
                .build().buildRegistry();
        container(1, List.of());
        var context = new GetContainerItemsContext(1, item -> true, true);

        assertTrue(registry.get(GetContainerItemsContext.class).execute(context).flowSucceeded());
        assertTrue(context.result().isEmpty());
    }

    private UOContainer container(int serial, List<Integer> contents) {
        var container = mock(UOContainer.class);
        when(container.getSerialId()).thenReturn(serial);
        when(container.getContainerItems()).thenReturn(contents);
        when(storage.getContainer(serial)).thenReturn(Optional.of(container));
        when(storage.getItem(serial)).thenReturn(Optional.of(container));
        return container;
    }

    private UOItem item(int serial) {
        var item = mock(UOItem.class);
        when(item.getSerialId()).thenReturn(serial);
        when(storage.getItem(serial)).thenReturn(Optional.of(item));
        return item;
    }
}
