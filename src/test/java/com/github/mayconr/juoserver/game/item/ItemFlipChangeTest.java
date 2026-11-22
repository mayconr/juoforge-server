package com.github.mayconr.juoserver.game.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mayconr.juoserver.game.item.flow.flip.FlipItemContext;
import com.github.mayconr.juoserver.game.item.flow.update.UpdateItemContext;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemFlipChangeTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void templatesAreNotFlippableByDefault() throws Exception {
        assertFalse(mapper.readValue("{\"name\":\"longsword\"}", ItemTemplate.class).flippable());
        assertFalse(new ItemTemplate("legacy", "Legacy", null, 1, true, 0,
                null, 0, List.of(), Map.of(), 0, null, null).flippable());
    }

    @Test
    void readsTheFlippableFlagFromJson() throws Exception {
        var template = mapper.readValue("{\"name\":\"longsword\",\"flippable\":true}", ItemTemplate.class);

        assertTrue(template.flippable());
        assertEquals(template, mapper.readValue(mapper.writeValueAsString(template), ItemTemplate.class));
    }

    @Test
    void aFlipCannotBeCombinedWithOtherChanges() {
        assertThrows(IllegalArgumentException.class, () -> new ItemChange(new PointInTheWorld(1, 2, 3), null, true));
        assertThrows(IllegalArgumentException.class, () -> new ItemChange(null, 42, true));
        assertFalse(ItemChange.flipModel().isEmpty());
        assertTrue(new ItemChange().isEmpty());
    }

    @Test
    void updateItemRunsTheFlipFlow() {
        var flows = mock(ModuleContext.FlowFacade.class);
        when(flows.execute(any())).thenReturn(StepResult.success());
        var item = mock(UOItem.class);

        module(flows).updateItem(item, ItemChange.flipModel());

        verify(flows).execute(any(FlipItemContext.class));
        verify(flows, never()).execute(any(UpdateItemContext.class));
    }

    @Test
    void aFailedFlipIsReported() {
        var flows = mock(ModuleContext.FlowFacade.class);
        when(flows.execute(any())).thenReturn(StepResult.failure("ITEM_NOT_FLIPPABLE", "not flippable"));

        var error = assertThrows(ModuleOperationException.class,
                () -> module(flows).updateItem(mock(UOItem.class), ItemChange.flipModel()));

        assertEquals("ITEM_NOT_FLIPPABLE", error.getCode());
    }

    private ItemModuleImpl module(ModuleContext.FlowFacade flows) {
        var module = new ItemModuleImpl(mock(WorldStorage.class), mock(ItemTemplateRegistry.class), mock(ItemUseService.class));
        var context = mock(ModuleContext.class);
        when(context.flows()).thenReturn(flows);
        module.initialize(context);
        return module;
    }
}
