package com.github.mayconr.juoserver.game.item.flow.flip;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.event.ItemUpdated;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import com.github.mayconr.juoserver.infrastructure.storage.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FlipItemFlowTest {
    private static final int BASE = 0xF60;

    private final WorldStorage storage = mock(WorldStorage.class);
    private final Items items = mock(Items.class);
    private final EventBus events = mock(EventBus.class);
    private final ItemTemplate template = mock(ItemTemplate.class);
    private final UOItem item = mock(UOItem.class);

    @BeforeEach
    void setUp() {
        when(storage.items()).thenReturn(items);
        when(template.flippable()).thenReturn(true);
        when(template.modelId()).thenReturn(BASE);
        when(item.getTemplate()).thenReturn(template);
        when(item.getModelId()).thenReturn(BASE);
        when(item.getCurrentLocation()).thenReturn(ItemLocation.ground());
    }

    @ParameterizedTest
    @CsvSource({"3936, 3937", "3937, 3936"})
    void alternatesBetweenTheTemplateModelAndTheNextOne(int current, int flipped) {
        when(item.getModelId()).thenReturn(current);

        assertTrue(flip().flowSucceeded());

        verify(item).setModelId(flipped);
    }

    @Test
    void refreshesAGroundItem() {
        flip();

        var event = published();
        assertSame(item, event.item());
        assertNull(event.container());
    }

    @Test
    void refreshesAnItemThroughItsContainer() {
        var container = mock(UOContainer.class);
        when(item.getCurrentLocation()).thenReturn(ItemLocation.container(7));
        when(items.getContainer(7)).thenReturn(Optional.of(container));

        assertTrue(flip().flowSucceeded());

        assertSame(container, published().container());
    }

    @Test
    void rejectsItemsThatAreNotFlippable() {
        when(template.flippable()).thenReturn(false);

        assertFailure("ITEM_NOT_FLIPPABLE");
    }

    @Test
    void rejectsItemsWithoutTemplate() {
        when(item.getTemplate()).thenReturn(null);

        assertFailure("ITEM_NOT_FLIPPABLE");
    }

    @Test
    void rejectsEquippedItems() {
        when(item.getCurrentLocation()).thenReturn(ItemLocation.equipped(5));

        assertFailure("ITEM_FLIP_LOCATION");
    }

    @Test
    void rejectsItemsWithoutPlace() {
        when(item.getCurrentLocation()).thenReturn(ItemLocation.orphan());

        assertFailure("ITEM_FLIP_LOCATION");
    }

    @Test
    void rejectsMissingContainers() {
        when(item.getCurrentLocation()).thenReturn(ItemLocation.container(7));
        when(items.getContainer(anyInt())).thenReturn(Optional.empty());

        assertFailure("ITEM_FLIP_CONTAINER");
    }

    @Test
    void rejectsModelsOutsideThePair() {
        when(item.getModelId()).thenReturn(BASE + 2);

        assertFailure("ITEM_FLIP_MODEL");
    }

    @Test
    void groundItemsCannotBecomeMultis() {
        when(template.modelId()).thenReturn(0x3FFF);
        when(item.getModelId()).thenReturn(0x3FFF);

        assertFailure("ITEM_FLIP_MODEL");
    }

    @Test
    void containerItemsUseTheWholeModelRange() {
        when(template.modelId()).thenReturn(0x4072);
        when(item.getModelId()).thenReturn(0x4072);
        when(item.getCurrentLocation()).thenReturn(ItemLocation.container(7));
        when(items.getContainer(7)).thenReturn(Optional.of(mock(UOContainer.class)));

        assertTrue(flip().flowSucceeded());

        verify(item).setModelId(0x4073);
    }

    private StepResult flip() {
        return FlipItemFlowDefinition.build(storage, events).execute(new FlipItemContext(item));
    }

    private ItemUpdated published() {
        var event = ArgumentCaptor.forClass(ItemUpdated.class);
        verify(events).publish(event.capture());
        return event.getValue();
    }

    private void assertFailure(String code) {
        var result = flip();

        assertTrue(result.flowFailed());
        assertEquals(code, result.code());
        verify(item, never()).setModelId(anyInt());
        verifyNoInteractions(events);
    }
}
