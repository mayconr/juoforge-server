package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationFlowDefinition;
import com.github.mayconr.juoserver.game.container.WorldContainer;
import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.item.flow.creation.factory.ApplyItemQualityStep;
import com.github.mayconr.juoserver.game.item.flow.creation.factory.CreateItemDataStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.ItemMaterial;
import com.github.mayconr.juoserver.game.model.ItemLocationType;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameInfra;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory.GameTemplates;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.storage.WorldStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemQualityTest {
    private static final ItemMaterial BASE = new ItemMaterial(100, 60, 40, 20);
    private final ApplyItemQualityStep step = new ApplyItemQualityStep();

    @ParameterizedTest
    @CsvSource({"-1, 0, 0, 0, 0", "-0.5, 50, 30, 20, 10", "0, 100, 60, 40, 20",
            "0.5, 150, 90, 60, 30", "1, 200, 120, 80, 40"})
    void adjustingFromNeutralScalesEveryPropertyAndStoresQuality(
            float adjustment, int density, int hardness, int toughness, int magic) {
        var context = context(ItemRequest.byName("ore").adjustQuality(adjustment), BASE);

        assertTrue(step.execute(context).shouldContinue());

        assertEquals(new ItemMaterial(density, hardness, toughness, magic), context.getData().getMaterial());
        assertEquals(adjustment, context.getData().getQuality());
    }

    @ParameterizedTest
    @ValueSource(floats = {-1, -0.5f, 0, 0.5f, 1})
    void declaredQualityIsStoredWithoutChangingTheMaterial(float quality) {
        var context = context(ItemRequest.byName("ore").withQuality(quality), BASE);

        step.execute(context);

        assertSame(BASE, context.getData().getMaterial());
        assertEquals(quality, context.getData().getQuality());
    }

    @Test
    void adjustmentChangesTheMaterialOnlyByTheQualityDifference() {
        var ore = new ItemMaterial(150, 90, 60, 30);
        var context = context(ItemRequest.byName("ingot").withQuality(0.5f).adjustQuality(-0.25f), ore);

        step.execute(context);

        assertEquals(0.25f, context.getData().getQuality());
        assertEquals(new ItemMaterial(125, 75, 50, 25), context.getData().getMaterial());
    }

    @Test
    void transferredQualityWithoutAdjustmentKeepsTheMaterial() {
        var ore = new ItemMaterial(150, 90, 60, 30);
        var context = context(ItemRequest.byName("ingot").withQuality(0.5f).adjustQuality(0), ore);

        step.execute(context);

        assertSame(ore, context.getData().getMaterial());
        assertEquals(0.5f, context.getData().getQuality());
    }

    @Test
    void chainedAdjustmentsDoNotCompound() {
        var mined = context(ItemRequest.byName("ore").adjustQuality(0.5f), BASE);
        step.execute(mined);
        var smelted = context(ItemRequest.byName("ingot").withQuality(0.5f).adjustQuality(-0.5f),
                mined.getData().getMaterial());

        step.execute(smelted);

        assertEquals(0.0f, smelted.getData().getQuality());
        assertEquals(BASE, smelted.getData().getMaterial());
    }

    @ParameterizedTest
    @CsvSource({"0.9, 0.5, 1", "-0.9, -0.5, -1", "0.5, 2, 1", "0.5, -2, -1"})
    void adjustedQualityIsClampedToItsRange(float quality, float adjustment, float expected) {
        var context = context(ItemRequest.byName("ore").withQuality(quality).adjustQuality(adjustment), null);

        step.execute(context);

        assertEquals(expected, context.getData().getQuality());
    }

    @Test
    void materialAtMinimumQualityCannotBeRecovered() {
        var zero = new ItemMaterial(0, 0, 0, 0);
        var context = context(ItemRequest.byName("ore").withQuality(-1).adjustQuality(1), zero);

        step.execute(context);

        assertSame(zero, context.getData().getMaterial());
        assertEquals(0.0f, context.getData().getQuality());
    }

    @Test
    void adjustmentsAccumulate() {
        var request = ItemRequest.byName("ore").adjustQuality(0.25f).adjustQuality(0.5f);

        assertEquals(0.75f, request.qualityAdjustment());
    }

    @Test
    void creationFlowAppliesQualityBeforeCompletingTheItem() {
        var template = mock(ItemTemplate.class);
        var data = new UOItemData();
        data.setMaterial(BASE);
        when(template.toData(anyInt())).thenReturn(data);
        var context = new ItemCreationContext(ItemRequest.byTemplate(template).adjustQuality(0.5f), ItemTarget.orphan());
        var infra = GameInfra.builder()
                .storage(mock(WorldStorage.class, RETURNS_DEEP_STUBS))
                .serialGenerator(mock(SerialGenerator.class))
                .eventBus(mock(EventBus.class))
                .build();
        var flow = ItemCreationFlowDefinition.build(infra, GameTemplates.builder().build(),
                mock(WorldContainer.class), mock(WorldMobile.class));

        assertTrue(flow.execute(context).shouldContinue());

        assertEquals(0.5f, context.getData().getQuality());
        assertEquals(new ItemMaterial(150, 90, 60, 30), context.getData().getMaterial());
    }

    @Test
    void roundsToNearestIntegerWithHalvesUp() {
        var context = context(ItemRequest.byName("ore").adjustQuality(0.5f), new ItemMaterial(1, 3, 5, 7));

        step.execute(context);

        assertEquals(new ItemMaterial(2, 5, 8, 11), context.getData().getMaterial());
    }

    @Test
    void neutralQualityPreservesTheMaterialInstance() {
        var context = context(ItemRequest.byName("ore"), BASE);

        step.execute(context);

        assertSame(BASE, context.getData().getMaterial());
    }

    @Test
    void absentMaterialRemainsAbsentButQualityIsStored() {
        var context = context(ItemRequest.byName("sword").adjustQuality(0.5f), null);

        step.execute(context);

        assertNull(context.getData().getMaterial());
        assertEquals(0.5f, context.getData().getQuality());
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.01f, 1.01f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsQualityOutsideTheFiniteRange(float quality) {
        assertThrows(IllegalArgumentException.class, () -> ItemRequest.byName("ore").withQuality(quality));
    }

    @ParameterizedTest
    @ValueSource(floats = {-2.01f, 2.01f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
    void rejectsAdjustmentOutsideTheFiniteRange(float adjustment) {
        assertThrows(IllegalArgumentException.class, () -> ItemRequest.byName("ore").adjustQuality(adjustment));
    }

    @Test
    void requestModifiersPreserveQualityAdjustmentAndMaterial() {
        var request = ItemRequest.byName("ore").withQuality(0.5f).adjustQuality(0.25f)
                .withMaterial(BASE).withAmount(3).withHue(5).withDirection(Direction.WEST);

        assertEquals(0.5f, request.quality());
        assertEquals(0.25f, request.qualityAdjustment());
        assertSame(BASE, request.material());
        assertEquals(0.0f, new ItemRequest(null, null, "ore", null, 1, null).quality());
        assertEquals(0.0f, new ItemRequest(null, null, "ore", null, 1, null, BASE).qualityAdjustment());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void appliesAdjustmentAfterResolvingTemplateOrRequestedMaterial(boolean override) {
        var template = mock(ItemTemplate.class);
        var templateData = new UOItemData();
        templateData.setMaterial(BASE);
        when(template.toData(anyInt())).thenReturn(templateData);
        var request = ItemRequest.byTemplate(template).adjustQuality(0.5f);
        if (override) {
            request = request.withMaterial(new ItemMaterial(20, 40, 60, 80));
        }
        var context = new ItemCreationContext(request, ItemTarget.orphan());
        context.setTemplate(template);
        new CreateItemDataStep(mock(SerialGenerator.class)).execute(context);

        step.execute(context);

        assertEquals(override ? new ItemMaterial(30, 60, 90, 120) : new ItemMaterial(150, 90, 60, 30),
                context.getData().getMaterial());
        assertEquals(new ItemMaterial(100, 60, 40, 20), BASE);
    }

    @Test
    void dataRoundTripDoesNotApplyQualityAgain() {
        var context = context(ItemRequest.byName("ore").adjustQuality(0.5f), BASE);
        step.execute(context);

        var item = new UOItem(context.getData(), null);
        var restored = new UOItem(item.toData(), null);

        assertEquals(0.5f, restored.getQuality());
        assertEquals(new ItemMaterial(150, 90, 60, 30), restored.getMaterial());
        assertEquals(0.5f, restored.toData().getQuality());
    }

    @Test
    void stackingAveragesQualityAndEffectivePropertiesWithoutApplyingQualityAgain() {
        var first = context(ItemRequest.byName("ore").adjustQuality(0.5f), BASE);
        var second = context(ItemRequest.byName("ore").adjustQuality(-0.5f), BASE);
        step.execute(first);
        step.execute(second);
        first.getData().setAmount(3);
        second.getData().setAmount(1);
        var stack = new UOItem(first.getData(), null);
        var other = new UOItem(second.getData(), null);

        stack.absorbStack(other);

        assertEquals(0.25f, stack.getQuality());
        assertEquals(new ItemMaterial(125, 75, 50, 25), stack.getMaterial());
        assertEquals(0.25f, stack.toData().getQuality());
        assertEquals(-0.5f, other.getQuality());
    }

    private ItemCreationContext context(ItemRequest request, ItemMaterial material) {
        var context = new ItemCreationContext(request, ItemTarget.orphan());
        var data = new UOItemData();
        data.setLocationType(ItemLocationType.ORPHAN);
        data.setMaterial(material);
        context.setData(data);
        return context;
    }
}
