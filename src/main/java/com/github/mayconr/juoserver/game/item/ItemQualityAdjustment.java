package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.ItemMaterial;

/**
 * Quality rules shared by item creation. Quality lies in [-1, 1] and a material reflects it through the
 * factor {@code 1 + quality}, so zero quality leaves the material unchanged.
 */
public final class ItemQualityAdjustment {
    public static final float MIN_QUALITY = -1.0f;
    public static final float MAX_QUALITY = 1.0f;
    /** Largest adjustment that can matter: it moves any quality to either bound. */
    public static final float MAX_ADJUSTMENT = MAX_QUALITY - MIN_QUALITY;

    private ItemQualityAdjustment() {
    }

    /** Raises or lowers {@code quality} by {@code adjustment}, clamping the result to [-1, 1]. */
    public static float adjust(float quality, float adjustment) {
        return Math.clamp(quality + adjustment, MIN_QUALITY, MAX_QUALITY);
    }

    /**
     * Moves a material that reflects quality {@code from} to quality {@code to}, changing it only by the
     * difference between both qualities. Equal qualities return the same instance. A material at quality -1
     * has factor zero and no recoverable proportions, so it is returned unchanged.
     *
     * @return the rescaled material, or {@code null} when {@code material} is {@code null}
     */
    public static ItemMaterial rescale(ItemMaterial material, float from, float to) {
        if (material == null || from == to || factor(from) == 0.0) {
            return material;
        }
        return material.scaled(factor(to) / factor(from));
    }

    private static double factor(float quality) {
        return 1.0 + quality;
    }
}
