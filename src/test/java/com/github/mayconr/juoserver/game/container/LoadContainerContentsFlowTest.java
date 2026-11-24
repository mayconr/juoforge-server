package com.github.mayconr.juoserver.game.container;

import com.github.mayconr.juoserver.game.container.flow.load.LoadContainerContentsContext;
import com.github.mayconr.juoserver.game.container.flow.load.LoadContainerContentsFlowDefinition;
import com.github.mayconr.juoserver.game.item.ContainerHandler;
import com.github.mayconr.juoserver.game.model.DefaultAttributeMap;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowFacade;
import com.github.mayconr.juoserver.game.world.context.DefaultFlowRegistry;
import com.github.mayconr.juoserver.game.world.context.DefaultModuleContext;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoadContainerContentsFlowTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final EventBus events = mock(EventBus.class);
    private final ContainerModuleImpl module =
            new ContainerModuleImpl(storage, new ContainerHandler(events, storage));

    @BeforeEach
    void initializeModule() {
        when(storage.items()).thenReturn(items);
        var registry = new DefaultFlowRegistry();
        registry.register("load-container-contents",
                LoadContainerContentsFlowDefinition.build(storage), LoadContainerContentsContext.class);
        module.initialize(new DefaultModuleContext(new DefaultFlowFacade(registry)));
    }

    @Test
    void registersLoadedChildrenWithoutPublishingEvents() {
        var container = container(1);
        var first = item(2);
        var second = item(3);
        when(items.loadContainerItems(container))
                .thenReturn(CompletableFuture.completedFuture(List.of(first, second)));

        var loaded = module.loadContents(container).join();

        assertEquals(List.of(first, second), loaded);
        assertEquals(Set.of(2, 3), Set.copyOf(container.getContainerItems()));
        verifyNoInteractions(events);
    }

    @Test
    void registersChildrenOnlyWhenLoadCompletes() {
        var container = container(1);
        var pending = new CompletableFuture<List<UOItem>>();
        when(items.loadContainerItems(container)).thenReturn(pending);

        var loaded = module.loadContents(container);

        assertFalse(loaded.isDone());
        assertTrue(container.getContainerItems().isEmpty());
        pending.complete(List.of(item(2)));
        assertEquals(Set.of(2), Set.copyOf(container.getContainerItems()));
    }

    @Test
    void rejectsMissingContainer() {
        assertThrows(NullPointerException.class, () -> module.loadContents(null));
    }

    private static UOContainer container(int serial) {
        return new UOContainer(data(serial, ItemLocationType.GROUND, List.of(ItemFlag.CONTAINER)), null);
    }

    private static UOItem item(int serial) {
        return new UOItem(data(serial, ItemLocationType.CONTAINER, List.of()), null);
    }

    private static UOItemData data(int serial, ItemLocationType location, List<ItemFlag> flags) {
        var data = new UOItemData();
        data.setSerialId(serial);
        data.setContainerSerialId(1);
        data.setLocationType(location);
        data.setFlags(flags);
        data.setPersistentAttrMap(new DefaultAttributeMap());
        return data;
    }
}
