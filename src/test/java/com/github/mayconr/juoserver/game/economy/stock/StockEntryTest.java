package com.github.mayconr.juoserver.game.economy.stock;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class StockEntryTest {
    @Test
    void initializesStockWithoutProductionOrConsumption() {
        var template = mock(ItemTemplate.class);
        var stock = new StockEntry(template, 100, 0.8, 40);

        assertSame(template, stock.getItemTemplate());
        assertEquals(100, stock.getMaxStock());
        assertEquals(0.8, stock.getElasticity());
        assertEquals(40, stock.getCurrentStock());
        assertEquals(0, stock.getProducedUnits());
        assertEquals(0, stock.getConsumedUnits());
    }

    @Test
    void accumulatesProductionAndConsumptionIndependently() {
        var stock = new StockEntry(mock(ItemTemplate.class), 100, 1, 40);

        stock.increaseProduction(10);
        stock.increaseProduction(20);
        stock.increaseConsumption(5);
        stock.increaseConsumption(15);

        assertEquals(50, stock.getCurrentStock());
        assertEquals(30, stock.getProducedUnits());
        assertEquals(20, stock.getConsumedUnits());
        assertEquals(0.5, stock.getScarcityFactor(), 1e-9);
    }

    @Test
    void capsStockButCountsAllProduction() {
        var stock = new StockEntry(mock(ItemTemplate.class), 10, 1, 8);

        stock.increaseProduction(5);
        stock.increaseProduction(2);

        assertEquals(10, stock.getCurrentStock());
        assertEquals(7, stock.getProducedUnits());
        assertEquals(0, stock.getConsumedUnits());
    }

    @Test
    void floorsStockAtZeroButCountsAllConsumption() {
        var stock = new StockEntry(mock(ItemTemplate.class), 10, 1, 3);

        stock.increaseConsumption(5);
        stock.increaseConsumption(2);

        assertEquals(0, stock.getCurrentStock());
        assertEquals(7, stock.getConsumedUnits());
        assertEquals(0, stock.getProducedUnits());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
    void ignoresNonPositiveProductionAndConsumption(int amount) {
        var stock = new StockEntry(mock(ItemTemplate.class), 100, 1, 40);

        stock.increaseProduction(amount);
        stock.increaseConsumption(amount);

        assertEquals(40, stock.getCurrentStock());
        assertEquals(0, stock.getProducedUnits());
        assertEquals(0, stock.getConsumedUnits());
    }

    @ParameterizedTest
    @CsvSource({"0, 1.0", "25, 0.75", "50, 0.5", "100, 0.0"})
    void calculatesScarcityFromAvailableStock(int amount, double expected) {
        var stock = new StockEntry(mock(ItemTemplate.class), 100, 1, amount);

        assertEquals(expected, stock.getScarcityFactor(), 1e-9);
    }

    @Test
    void reportsFullScarcityWhenMaximumStockIsZero() {
        var stock = new StockEntry(mock(ItemTemplate.class), 0, 1, 0);

        assertEquals(1.0, stock.getScarcityFactor());
    }
}
