package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.GamePlaySettings.ItemQuality;
import com.github.mayconr.juoserver.game.model.UOMobile;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/**
 * Rolls quality using a modifier and signed luck, preserving the modifier's guaranteed endpoints.
 * Out-of-range modifier and luck are clamped to the nearest bound of the configured {@link ItemQuality} ranges.
 */
public final class ItemQualityCalculator {
    private final ItemQuality ranges;
    private final DoubleSupplier random;

    public ItemQualityCalculator() {
        this(new ItemQuality());
    }

    public ItemQualityCalculator(ItemQuality ranges) {
        this(ranges, () -> ThreadLocalRandom.current().nextDouble());
    }

    public ItemQualityCalculator(DoubleSupplier random) {
        this(new ItemQuality(), random);
    }

    public ItemQualityCalculator(ItemQuality ranges, DoubleSupplier random) {
        this.ranges = Objects.requireNonNull(ranges, "ranges");
        this.random = Objects.requireNonNull(random, "random");
    }

    public float calculate(UOMobile mobile, double modifier) {
        Objects.requireNonNull(mobile, "mobile");
        double base = normalizeModifier(modifier);
        if (base == 0.0) {
            return -1.0f;
        }
        if (base == 1.0) {
            return 1.0f;
        }
        double mean = base + normalizeLuck(mobile.getLuck()) * base * (1.0 - base);
        return (float) (2.0 * Math.pow(sample(), (1.0 - mean) / mean) - 1.0);
    }

    /** Maps the clamped modifier to [0, 1]. */
    private double normalizeModifier(double modifier) {
        if (Double.isNaN(modifier)) {
            throw new IllegalArgumentException("modifier must be a number");
        }
        double clamped = Math.clamp(modifier, ranges.minModifier(), ranges.maxModifier());
        return (clamped - ranges.minModifier()) / (ranges.maxModifier() - ranges.minModifier());
    }

    /** Maps the clamped luck to [-1, 1], keeping zero luck neutral. */
    private double normalizeLuck(int luck) {
        int clamped = Math.clamp(luck, ranges.minLuck(), ranges.maxLuck());
        if (clamped > 0) {
            return (double) clamped / ranges.maxLuck();
        }
        if (clamped < 0) {
            return (double) clamped / -ranges.minLuck();
        }
        return 0.0;
    }

    private double sample() {
        double roll = random.getAsDouble();
        if (!Double.isFinite(roll) || roll < 0.0 || roll >= 1.0) {
            throw new IllegalStateException("random sample must be finite and between 0 (inclusive) and 1 (exclusive)");
        }
        return roll;
    }
}
