package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.GamePlaySettings.ItemQuality;
import com.github.mayconr.juoserver.game.model.UOMobile;
import java.util.Random;
import java.util.function.DoubleSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemQualityCalculatorTest {
    @ParameterizedTest
    @CsvSource({"-100, 0, -1", "0, 0, -1", "100, 0, -1",
            "-100, 100, 1", "0, 100, 1", "100, 100, 1"})
    void modifierEndpointsIgnoreLuckAndDoNotRoll(int luck, double modifier, float expected) {
        var random = mock(DoubleSupplier.class);

        assertEquals(expected, new ItemQualityCalculator(random).calculate(mobile(luck), modifier));

        verifyNoInteractions(random);
    }

    @ParameterizedTest
    @CsvSource({"0, -1", "0.25, -0.5", "0.5, 0", "0.75, 0.5"})
    void neutralLuckAndMidpointModifierProduceUniformQuality(double roll, float expected) {
        assertEquals(expected, new ItemQualityCalculator(() -> roll).calculate(mobile(0), 50));
    }

    @Test
    void luckAndModifierImproveTheSameRollMonotonically() {
        var calculator = new ItemQualityCalculator(() -> 0.5);

        assertTrue(calculator.calculate(mobile(-100), 50) < calculator.calculate(mobile(0), 50));
        assertTrue(calculator.calculate(mobile(100), 50) > calculator.calculate(mobile(0), 50));
        assertTrue(calculator.calculate(mobile(0), 25) < calculator.calculate(mobile(0), 50));
        assertTrue(calculator.calculate(mobile(0), 75) > calculator.calculate(mobile(0), 50));
    }

    @ParameterizedTest
    @CsvSource({"-0.01, -1", "-500, -1", "-Infinity, -1", "100.01, 1", "500, 1", "Infinity, 1"})
    void clampsOutOfRangeModifiersToTheirGuaranteedEndpoints(double modifier, float expected) {
        var random = mock(DoubleSupplier.class);

        assertEquals(expected, new ItemQualityCalculator(random).calculate(mobile(0), modifier));

        verifyNoInteractions(random);
    }

    @Test
    void rejectsNaNModifier() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemQualityCalculator(() -> 0.5).calculate(mobile(0), Double.NaN));
    }

    @ParameterizedTest
    @CsvSource({"-101, -100", "-1000, -100", "101, 100", "1000, 100"})
    void clampsOutOfRangeLuckToTheNearestBound(int luck, int clamped) {
        var calculator = new ItemQualityCalculator(() -> 0.5);

        assertEquals(calculator.calculate(mobile(clamped), 50), calculator.calculate(mobile(luck), 50));
    }

    @ParameterizedTest
    @CsvSource({"10, -1", "5, -1", "30, 1", "40, 1", "20, 0"})
    void configuredModifierRangeDefinesEndpointsAndMidpoint(double modifier, float expected) {
        var calculator = new ItemQualityCalculator(new ItemQuality(10, 30, -100, 100), () -> 0.5);

        assertEquals(expected, calculator.calculate(mobile(0), modifier));
    }

    @Test
    void configuredLuckRangeNormalizesEachSignToItsBound() {
        var configured = new ItemQualityCalculator(new ItemQuality(0, 100, -50, 200), () -> 0.5);
        var defaults = new ItemQualityCalculator(() -> 0.5);

        assertEquals(defaults.calculate(mobile(-100), 50), configured.calculate(mobile(-50), 50));
        assertEquals(defaults.calculate(mobile(100), 50), configured.calculate(mobile(200), 50));
        assertEquals(defaults.calculate(mobile(50), 50), configured.calculate(mobile(100), 50));
        assertEquals(defaults.calculate(mobile(-100), 50), configured.calculate(mobile(-80), 50));
    }

    @Test
    void luckRangeWithoutPositiveSideIgnoresPositiveLuck() {
        var calculator = new ItemQualityCalculator(new ItemQuality(0, 100, -100, 0), () -> 0.5);

        assertEquals(calculator.calculate(mobile(0), 50), calculator.calculate(mobile(100), 50));
    }

    @Test
    void publicWorldItemMethodReturnsQualityReadyForARequestWithoutInitializingFlows() {
        WorldItem items = new ItemModuleImpl(null, null, null, new ItemQualityCalculator(() -> 0.5));

        var request = ItemRequest.byName("ingot").withQuality(items.calculateQuality(mobile(100), 50));

        assertEquals((float) (2 * Math.pow(0.5, 1.0 / 3.0) - 1), request.quality(), 0.000001f);
    }

    @Test
    void seededSamplesStayInRangeAndHaveTheDocumentedMean() {
        for (int luck : new int[]{-100, 0, 100}) {
            var mobile = mobile(luck);
            var seeded = new Random(12345);
            var calculator = new ItemQualityCalculator(seeded::nextDouble);
            for (double modifier : new double[]{0.001, 25, 50, 75, 99.999}) {
                double sum = 0;
                for (int i = 0; i < 10000; i++) {
                    float quality = calculator.calculate(mobile, modifier);
                    assertTrue(Float.isFinite(quality) && quality >= -1 && quality <= 1);
                    sum += quality;
                }
                double base = modifier / 100;
                double expected = 2 * (base + luck / 100.0 * base * (1 - base)) - 1;
                assertEquals(expected, sum / 10000, 0.025);
            }
        }
    }

    private UOMobile mobile(int luck) {
        var mobile = mock(UOMobile.class);
        when(mobile.getLuck()).thenReturn(luck);
        return mobile;
    }
}
