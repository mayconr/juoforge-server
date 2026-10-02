package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemDeleted;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemModuleTest {
    private final RealmStorage storage = mock(RealmStorage.class);
    private final EventBus events = mock(EventBus.class);
    private final ItemModule module = new ItemModuleImpl(new ItemHandler(storage, events),
            new ContainerHandler(events, storage), storage, mock(ItemTemplateRegistry.class));

    @Test
    void deletesResolvedItemAndPublishesEvent() {
        int serial = 0x40000001;
        var item = mock(UOItem.class);
        when(storage.getItem(serial)).thenReturn(Optional.of(item));
        module.deleteItem(serial);
        verify(storage).deleteItem(item);
        verify(events).publish(new ItemDeleted(item));
    }

    @Test
    void rejectsMobileSerialBeforeAccessingStorage() {
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(1));
        verifyNoInteractions(storage, events);
    }

    @Test
    void missingItemDoesNotPublishDeletion() {
        int serial = 0x40000001;
        when(storage.getItem(serial)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(serial));
        verifyNoInteractions(events);
        verify(storage, never()).deleteItem(any());
    }
}
