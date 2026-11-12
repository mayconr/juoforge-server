package com.github.mayconr.juoserver.game.model;

/**
 * Physical properties of a raw material carried by an item instance.
 *
 * @param density       mass per volume of the material
 * @param hardness      resistance to scratching and deformation
 * @param toughness     resistance to fracture under impact
 * @param magicAffinity how readily the material holds magic
 */
public record ItemMaterial(int density, int hardness, int toughness, int magicAffinity) {

    /**
     * Material of two merged stacks: each component is the average weighted by the stack amounts, rounded to
     * the nearest integer (halves up). A stack without material contributes no value, so the result is the other
     * stack's material; when neither has one, the result has none.
     */
    public static ItemMaterial merge(ItemMaterial first, int firstAmount, ItemMaterial second, int secondAmount) {
        if (first == null || second == null) {
            return first == null ? second : first;
        }
        if (firstAmount <= 0 || secondAmount <= 0) {
            throw new IllegalArgumentException("Stack amounts must be positive");
        }
        return new ItemMaterial(
                weightedAverage(first.density, firstAmount, second.density, secondAmount),
                weightedAverage(first.hardness, firstAmount, second.hardness, secondAmount),
                weightedAverage(first.toughness, firstAmount, second.toughness, secondAmount),
                weightedAverage(first.magicAffinity, firstAmount, second.magicAffinity, secondAmount)
        );
    }

    /**
     * Multiplies every property by {@code factor}, rounding to the nearest integer with halves up.
     *
     * @throws ArithmeticException if a scaled property does not fit in an {@code int}
     */
    public ItemMaterial scaled(double factor) {
        return new ItemMaterial(
                scale(density, factor),
                scale(hardness, factor),
                scale(toughness, factor),
                scale(magicAffinity, factor)
        );
    }

    private static int scale(int value, double factor) {
        return Math.toIntExact(Math.round(value * factor));
    }

    private static int weightedAverage(int first, int firstAmount, int second, int secondAmount) {
        long total = (long) firstAmount + secondAmount;
        long weightedSum = Math.addExact((long) first * firstAmount, (long) second * secondAmount);
        return Math.toIntExact(Math.floorDiv(Math.addExact(weightedSum, total / 2), total));
    }
}
