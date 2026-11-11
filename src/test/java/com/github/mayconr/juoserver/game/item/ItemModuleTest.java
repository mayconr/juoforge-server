package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.delete.DeleteItemContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.Trigger;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext.FlowFacade;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemModuleTest {
    private final WorldStorage storage = mock(WorldStorage.class);
    private final FlowFacade flows = mock(FlowFacade.class);
    private final ItemUseService itemUse = mock(ItemUseService.class);
    private final ItemModule module = new ItemModuleImpl(storage, mock(ItemTemplateRegistry.class), itemUse);

    @BeforeEach
    void setUp() {
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(flows);
        module.initialize(context);
    }

    @Test
    void dispatchesItemUseToTheService() {
        var context = new ItemUseContext(mock(UOPlayer.class), mock(UOItem.class), Trigger.DOUBLE_CLICK);
        module.use(context);
        verify(itemUse).use(context);
    }

    @Test
    void deletesResolvedItemThroughTheDeletionFlow() {
        int serial = 0x40000001;
        var item = mock(UOItem.class);
        when(storage.getItem(serial)).thenReturn(Optional.of(item));
        when(flows.execute(any(DeleteItemContext.class))).thenReturn(StepResult.success());

        module.deleteItem(serial);

        var context = ArgumentCaptor.forClass(DeleteItemContext.class);
        verify(flows).execute(context.capture());
        assertSame(item, context.getValue().getItem());
    }

    @Test
    void failedDeletionFlowRaisesModuleError() {
        when(flows.execute(any(DeleteItemContext.class))).thenReturn(StepResult.failure("FAILED", "failed"));

        assertThrows(ModuleOperationException.class, () -> module.deleteItem(mock(UOItem.class)));
    }

    @Test
    void rejectsMobileSerialBeforeAccessingStorage() {
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(1));
        verifyNoInteractions(storage, flows);
    }

    @Test
    void missingItemDoesNotRunTheDeletionFlow() {
        int serial = 0x40000001;
        when(storage.getItem(serial)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> module.deleteItem(serial));
        verifyNoInteractions(flows);
    }
}
