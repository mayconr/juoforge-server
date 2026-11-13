package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.factory.CreateItemDataStep;
import com.github.mayconr.juoserver.game.item.flow.creation.template.ResolveItemTileDataStep;
import com.github.mayconr.juoserver.game.item.flow.creation.validation.ValidateItemAmountStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.datafile.ItemTileInfo;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemStackabilityTest {
    private static final int STACKABLE_INGOT = 0x1BF2;
    private static final int INGOT_PILE = 0x1BF3;

    private final UOFileReader reader = mock(UOFileReader.class);
    private final SerialGenerator serials = mock(SerialGenerator.class);

    @Test
    void createdItemIsStackableWhenTiledataMarksTheGraphicAsStackable() {
        stubTiledata(STACKABLE_INGOT, true);
        var context = create(template(STACKABLE_INGOT, List.of()));

        assertTrue(context.isStackable());
        assertEquals(List.of(ItemFlag.STACKABLE), context.getData().getFlags());
    }

    @Test
    void createdItemIsNotStackableWhenTiledataDoesNotMarkTheGraphic() {
        stubTiledata(INGOT_PILE, false);
        var context = create(template(INGOT_PILE, List.of(ItemFlag.WEARABLE)));

        assertFalse(context.isStackable());
        assertEquals(List.of(ItemFlag.WEARABLE), context.getData().getFlags());
    }

    @Test
    void itemIsNotStackableWithoutFileReaderOrTiledataEntry() {
        when(reader.getItemTileInfo(INGOT_PILE)).thenReturn(Optional.empty());
        when(reader.isStackable(INGOT_PILE)).thenCallRealMethod();

        assertFalse(create(template(INGOT_PILE, List.of())).isStackable());
        var withoutReader = context(template(STACKABLE_INGOT, List.of()));
        new ResolveItemTileDataStep(null).execute(withoutReader);
        assertFalse(withoutReader.isStackable());
    }

    @Test
    void templatesCannotDeclareStackable() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> template(STACKABLE_INGOT, List.of(ItemFlag.STACKABLE)));
        assertTrue(error.getMessage().contains("iron_ingot"));
    }

    @Test
    void rejectsAmountAboveOneForNonStackableGraphic() {
        stubTiledata(INGOT_PILE, false);
        var context = resolve(template(INGOT_PILE, List.of()), 2);

        var result = new ValidateItemAmountStep().execute(context);

        assertTrue(result.flowFailed());
        assertEquals("ITEM_NOT_STACKABLE", result.code());
    }

    @Test
    void acceptsSingleNonStackableItemAndStacksOfStackableGraphic() {
        stubTiledata(INGOT_PILE, false);
        stubTiledata(STACKABLE_INGOT, true);

        assertTrue(new ValidateItemAmountStep().execute(resolve(template(INGOT_PILE, List.of()), 1)).shouldContinue());
        assertTrue(new ValidateItemAmountStep()
                .execute(resolve(template(STACKABLE_INGOT, List.of()), 50)).shouldContinue());
    }

    private ItemCreationContext resolve(ItemTemplate template, int amount) {
        var context = new ItemCreationContext(ItemRequest.byTemplate(template).withAmount(amount),
                ItemTarget.orphan(), null);
        context.setTemplate(template);
        new ResolveItemTileDataStep(reader).execute(context);
        return context;
    }

    private void stubTiledata(int modelId, boolean stackable) {
        when(reader.getItemTileInfo(modelId)).thenReturn(Optional.of(new ItemTileInfo(
                modelId, "iron ingot", stackable, Set.of(), 0, 0, 0, 0, 0, 0, 1, 1)));
        when(reader.isStackable(modelId)).thenCallRealMethod();
    }

    private ItemCreationContext create(ItemTemplate template) {
        var context = context(template);
        assertTrue(new ResolveItemTileDataStep(reader).execute(context).shouldContinue());
        assertTrue(new CreateItemDataStep(serials).execute(context).shouldContinue());
        return context;
    }

    private ItemCreationContext context(ItemTemplate template) {
        var context = new ItemCreationContext(ItemRequest.byTemplate(template), ItemTarget.orphan(), null);
        context.setTemplate(template);
        return context;
    }

    private ItemTemplate template(int modelId, List<ItemFlag> flags) {
        return new ItemTemplate("iron_ingot", "iron ingots", null, modelId, true, 0, null, 0,
                flags, Map.of(), 0, null, null);
    }
}
