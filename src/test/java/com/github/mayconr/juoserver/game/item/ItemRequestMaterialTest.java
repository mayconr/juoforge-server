package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.item.flow.creation.factory.CreateItemDataStep;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.Material;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.ItemMaterial;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemRequestMaterialTest {
    private static final ItemMaterial BRONZE = new ItemMaterial(80, 70, 60, 5);
    private static final ItemMaterial IRON = new ItemMaterial(70, 60, 65, 10);

    @Nested
    class Request {

        @Test
        void factoriesInheritTheTemplateMaterial() {
            assertNull(ItemRequest.byTemplate(template(null)).material());
            assertNull(ItemRequest.byName("iron_ore").material());
            assertNull(ItemRequest.byModelId(6585).material());
        }

        @Test
        void constructorWithoutMaterialInheritsTheTemplateMaterial() {
            var request = new ItemRequest(null, null, "iron_ore", 5, 3, Direction.SOUTH);

            assertNull(request.material());
        }

        @Test
        void withMaterialKeepsEveryOtherField() {
            var original = ItemRequest.byName("iron_ore").withHue(0x455).withAmount(7).withDirection(Direction.EAST);

            var request = original.withMaterial(BRONZE);

            assertAll(
                    () -> assertEquals(BRONZE, request.material()),
                    () -> assertEquals("iron_ore", request.name()),
                    () -> assertEquals(0x455, request.hue()),
                    () -> assertEquals(7, request.amount()),
                    () -> assertEquals(Direction.EAST, request.direction()),
                    () -> assertNull(original.material())
            );
        }

        @Test
        void otherWithersKeepTheMaterial() {
            var request = ItemRequest.byName("iron_ore").withMaterial(BRONZE);

            assertEquals(BRONZE, request.withHue(1).material());
            assertEquals(BRONZE, request.withAmount(2).material());
            assertEquals(BRONZE, request.withDirection(Direction.WEST).material());
        }

        @Test
        void nullMaterialReturnsToTheTemplateMaterial() {
            var request = ItemRequest.byName("iron_ore").withMaterial(BRONZE).withMaterial(null);

            assertNull(request.material());
        }

        @Test
        void derivedFromCopiesHueMaterialAndQualityAndKeepsDefaults() {
            var source = source(0x455, BRONZE);
            when(source.getQuality()).thenReturn(0.3f);

            var request = ItemRequest.derivedFrom(source, "iron_ingot");

            assertAll(
                    () -> assertEquals("iron_ingot", request.name()),
                    () -> assertEquals(0x455, request.hue()),
                    () -> assertEquals(BRONZE, request.material()),
                    () -> assertEquals(0.3f, request.quality()),
                    () -> assertEquals(0.0f, request.qualityAdjustment()),
                    () -> assertEquals(1, request.amount()),
                    () -> assertNull(request.direction())
            );
        }

        @Test
        void derivedFromCopiesSourceWithoutColorAsExplicitZeroHue() {
            assertEquals(0, ItemRequest.derivedFrom(source(0, BRONZE), "iron_ingot").hue());
        }

        @Test
        void derivedFromSourceWithoutMaterialInheritsTheTemplateMaterial() {
            assertNull(ItemRequest.derivedFrom(source(0x455, null), "iron_ingot").material());
        }

        @Test
        void derivedFromRequiresSource() {
            assertThrows(NullPointerException.class, () -> ItemRequest.derivedFrom(null, "iron_ingot"));
        }

        @Test
        void derivedFromRequiresTemplateName() {
            assertThrows(IllegalArgumentException.class, () -> ItemRequest.derivedFrom(source(0, BRONZE), " "));
        }
    }

    @Nested
    class Creation {
        private final SerialGenerator serials = mock(SerialGenerator.class);
        private final CreateItemDataStep step = new CreateItemDataStep(serials);

        @Test
        void requestWithoutMaterialKeepsTheTemplateMaterial() {
            var data = create(template(new Material(70, 60, 65, 10)), ItemRequest::byTemplate);

            assertEquals(IRON, data.getMaterial());
        }

        @Test
        void requestedMaterialReplacesTheTemplateMaterial() {
            var data = create(template(new Material(70, 60, 65, 10)), t -> ItemRequest.byTemplate(t).withMaterial(BRONZE));

            assertEquals(BRONZE, data.getMaterial());
        }

        @Test
        void requestedMaterialIsAppliedToTemplatesWithoutMaterial() {
            var data = create(template(null), t -> ItemRequest.byTemplate(t).withMaterial(BRONZE));

            assertEquals(BRONZE, data.getMaterial());
        }

        @Test
        void itemWithoutMaterialOnBothSidesHasNoMaterial() {
            var data = create(template(null), ItemRequest::byTemplate);

            assertNull(data.getMaterial());
        }

        @Test
        void materialDoesNotInterfereWithTheOtherRequestedValues() {
            var data = create(template(new Material(70, 60, 65, 10)), t -> ItemRequest.byTemplate(t)
                    .withMaterial(BRONZE)
                    .withHue(0x455)
                    .withAmount(9)
                    .withDirection(Direction.SOUTH));

            assertAll(
                    () -> assertEquals(BRONZE, data.getMaterial()),
                    () -> assertEquals(0x455, data.getHue()),
                    () -> assertEquals(9, data.getAmount()),
                    () -> assertEquals(Direction.SOUTH, data.getDirection()),
                    () -> assertEquals(0x40000099, data.getSerialId())
            );
        }

        @Test
        void derivedRequestReplacesTemplateHueAndMaterial() {
            var source = source(0x455, BRONZE);
            var data = create(template(new Material(70, 60, 65, 10)), t -> ItemRequest.derivedFrom(source, t.name()));

            assertAll(
                    () -> assertEquals(BRONZE, data.getMaterial()),
                    () -> assertEquals(0x455, data.getHue())
            );
        }

        @Test
        void requestedMaterialDoesNotChangeTheTemplate() {
            var template = template(new Material(70, 60, 65, 10));

            create(template, t -> ItemRequest.byTemplate(t).withMaterial(BRONZE));

            assertEquals(new Material(70, 60, 65, 10), template.material());
            assertEquals(IRON, template.toData(1).getMaterial());
        }

        private UOItemData create(ItemTemplate template, Function<ItemTemplate, ItemRequest> request) {
            when(serials.getNextItem()).thenReturn(0x40000099);
            var context = new ItemCreationContext(request.apply(template), ItemTarget.orphan());
            context.setTemplate(template);

            assertTrue(step.execute(context).flowSucceeded());
            return context.getData();
        }
    }

    private static UOItem source(int hue, ItemMaterial material) {
        var source = mock(UOItem.class);
        when(source.getHue()).thenReturn(hue);
        when(source.getMaterial()).thenReturn(material);
        return source;
    }

    private static ItemTemplate template(Material material) {
        return new ItemTemplate(
                "iron_ore", "iron ore", null, 6585, true, 0, "ORE", 10,
                List.of(), Map.of(), 0, null, null, null, 1, "OTHER", material
        );
    }
}
