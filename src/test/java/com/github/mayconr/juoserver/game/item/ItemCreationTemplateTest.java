package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.creation.*;
import com.github.mayconr.juoserver.game.item.flow.creation.template.TemplateLoaderStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import com.github.mayconr.juoserver.game.world.context.*;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.*;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemCreationTemplateTest {
    private final InMemoryTemplateRegistry<String, ItemTemplate> names =
            new InMemoryTemplateRegistry<>(List.of(), ItemTemplate::name);
    private final InMemoryTemplateRegistry<Integer, ItemTemplate> models =
            new InMemoryTemplateRegistry<>(List.of(), ItemTemplate::modelId);
    private final RealmStorage storage = mock(RealmStorage.class);
    private final EventBus events = mock(EventBus.class);
    private final SerialGenerator serials = mock(SerialGenerator.class);

    private com.github.mayconr.juoserver.infrastructure.flow.Flow<ItemCreationContext> flow() {
        return ItemCreationFlowDefinition.build(GameInfra.builder().storage(storage).eventBus(events)
                        .serialGenerator(serials).build(),
                GameTemplates.builder().itemByName(names).itemByModelId(models).build());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void missingNameOrModelReturnsExplicitFailureWithoutCreatingItem(boolean byName) {
        var request = byName ? ItemRequest.byName("missing_item") : ItemRequest.byModelId(999);

        var result = flow().execute(new ItemCreationContext(request, ItemTarget.orphan()));

        assertTrue(result.flowFailed());
        assertEquals("ITEM_TEMPLATE_NOT_FOUND", result.code());
        assertEquals("{item.template_not_found}", result.displayMessage());
        assertTrue(result.reason().contains(byName ? "missing_item" : "999"));
        verifyNoInteractions(storage, events, serials);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void bothModuleOverloadsPreserveTemplateFailure(boolean withOptions) {
        var registry = new DefaultFlowRegistry();
        registry.register("create-item", flow(), ItemCreationContext.class);
        var module = new ItemModuleImpl(null, null, storage, null);
        module.initialize(DefaultModuleContext.builder()
                .flowFacade(DefaultFlowFacade.builder().registry(registry).build()).build());
        var request = ItemRequest.byName("missing_item");

        var error = assertThrows(ModuleOperationException.class, () -> {
            if (withOptions) module.createItem(request, ItemTarget.orphan(), options -> options.renderOnCreate(false));
            else module.createItem(request, ItemTarget.orphan());
        });

        assertEquals("ITEM_TEMPLATE_NOT_FOUND", error.getCode());
        assertEquals("createItem", error.getOperation());
        assertEquals("{item.template_not_found}", error.getDisplayMessage());
        assertTrue(error.getMessage().contains("missing_item"));
        verifyNoInteractions(storage, events, serials);
    }

    @Test
    void explicitTemplateIsResolvedWithoutRegistryLookup() {
        var template = mock(ItemTemplate.class);
        var context = new ItemCreationContext(ItemRequest.byTemplate(template), ItemTarget.orphan());

        assertTrue(new TemplateLoaderStep(names, models).execute(context).flowSucceeded());
        assertSame(template, context.getTemplate());
    }
}
