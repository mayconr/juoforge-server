package com.github.mayconr.juoserver.game.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemMaterialMergeTest {
    private static final ItemMaterial IRON = new ItemMaterial(70, 60, 65, 10);
    private static final ItemMaterial BRONZE = new ItemMaterial(80, 70, 60, 5);

    @Test
    void averagesEachComponentWeightedByTheStackAmounts() {
        var merged = ItemMaterial.merge(new ItemMaterial(100, 0, 10, 40), 3, new ItemMaterial(0, 100, 50, 0), 1);

        assertEquals(new ItemMaterial(75, 25, 20, 30), merged);
    }

    @Test
    void equalAmountsGiveTheSimpleAverage() {
        assertEquals(new ItemMaterial(75, 65, 63, 8), ItemMaterial.merge(IRON, 5, BRONZE, 5));
    }

    @Test
    void equalMaterialsStayTheSameWhateverTheAmounts() {
        assertEquals(IRON, ItemMaterial.merge(IRON, 1, IRON, 999));
    }

    @Test
    void componentsAreNotMixedWithEachOther() {
        var merged = ItemMaterial.merge(new ItemMaterial(10, 0, 0, 0), 1, new ItemMaterial(0, 0, 0, 30), 1);

        assertEquals(new ItemMaterial(5, 0, 0, 15), merged);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 2, 2, 1, 1",     // (2 + 2) / 3 = 1.33
            "1, 1, 2, 2, 2",     // (1 + 4) / 3 = 1.67
            "-1, 2, -2, 1, -1",  // (-2 - 2) / 3 = -1.33
            "-1, 1, -2, 2, -2"   // (-1 - 4) / 3 = -1.67
    })
    void roundsToTheNearestInteger(int first, int firstAmount, int second, int secondAmount, int expected) {
        var merged = ItemMaterial.merge(
                new ItemMaterial(first, 0, 0, 0), firstAmount, new ItemMaterial(second, 0, 0, 0), secondAmount);

        assertEquals(expected, merged.density());
    }

    @Test
    void halvesRoundUp() {
        assertEquals(2, ItemMaterial.merge(new ItemMaterial(1, 0, 0, 0), 1, new ItemMaterial(2, 0, 0, 0), 1).density());
        assertEquals(-1, ItemMaterial.merge(new ItemMaterial(-1, 0, 0, 0), 1, new ItemMaterial(-2, 0, 0, 0), 1).density());
    }

    @Test
    void largeValuesAndAmountsDoNotOverflow() {
        var max = new ItemMaterial(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(max, ItemMaterial.merge(max, Integer.MAX_VALUE, max, Integer.MAX_VALUE));
    }

    @Test
    void stackWithoutMaterialKeepsTheOtherMaterial() {
        assertSame(IRON, ItemMaterial.merge(IRON, 5, null, 50));
        assertSame(IRON, ItemMaterial.merge(null, 50, IRON, 5));
    }

    @Test
    void stacksWithoutMaterialStayWithoutMaterial() {
        assertNull(ItemMaterial.merge(null, 3, null, 4));
    }

    @Test
    void rejectsNonPositiveAmountsWhenBothHaveMaterial() {
        for (var amounts : List.of(new int[] {0, 1}, new int[] {1, 0}, new int[] {-1, 5})) {
            assertThrows(IllegalArgumentException.class,
                    () -> ItemMaterial.merge(IRON, amounts[0], BRONZE, amounts[1]));
        }
    }
}
