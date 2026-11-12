package com.github.mayconr.juoserver.game.model;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate.Material;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UOItemMaterialTest {
    private static final Material IRON_TEMPLATE = new Material(70, 60, 65, 10);
    private static final ItemMaterial IRON = new ItemMaterial(70, 60, 65, 10);

    @Test
    void templateMaterialIsConvertedToTheItemMaterialOfTheCreatedData() {
        var data = template(IRON_TEMPLATE).toData(0x40000001);

        assertEquals(IRON, data.getMaterial());
    }

    @Test
    void materialSurvivesTheRoundTripBetweenDataAndItem() {
        var template = template(IRON_TEMPLATE);

        var item = new UOItem(template.toData(0x40000001), template);
        var restored = new UOItem(item.toData(), template);

        assertEquals(IRON, item.getMaterial());
        assertEquals(IRON, item.toData().getMaterial());
        assertEquals(IRON, restored.getMaterial());
    }

    @Test
    void itemWithoutMaterialKeepsItAbsentInBothDirections() {
        var template = template(null);

        var item = new UOItem(template.toData(0x40000001), template);

        assertNull(item.getMaterial());
        assertNull(item.toData().getMaterial());
    }

    @Test
    void itemMaterialCanBeReplacedAndIsWrittenBackToData() {
        var template = template(null);
        var item = new UOItem(template.toData(0x40000001), template);

        item.setMaterial(IRON);

        assertEquals(IRON, item.toData().getMaterial());
    }

    private static ItemTemplate template(Material material) {
        return new ItemTemplate(
                "iron_ore",
                "iron ore",
                null,
                6585,
                true,
                0,
                "ORE",
                10,
                List.of(),
                Map.of(),
                0,
                null,
                null,
                "mining.smelting",
                1,
                "OTHER",
                material
        );
    }
}
