package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.ItemMaterial;
import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.Objects;

/**
 * A null hue inherits the template hue; zero explicitly removes its color.
 * A null material inherits the template material; a material replaces it on the created item.
 * Quality is the quality the resolved material already reflects; it is stored as is and never changes the
 * material. Template materials reflect quality zero. The quality adjustment raises or lowers that quality,
 * clamped to [-1, 1], and changes the material only by the difference; zero leaves it unchanged.
 */
public record ItemRequest(
        ItemTemplate template,
        Integer modelId,
        String name,
        Integer hue,
        int amount,
        Direction direction,
        ItemMaterial material,
        float quality,
        float qualityAdjustment
) {
    public ItemRequest {
        int count = 0;

        if (template != null) count++;
        if (modelId != null) count++;
        if (name != null && !name.isBlank()) count++;

        if (count != 1) {
            throw new IllegalArgumentException("Exactly one of template, modelId or name must be provided");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        if (!Float.isFinite(quality)
                || quality < ItemQualityAdjustment.MIN_QUALITY || quality > ItemQualityAdjustment.MAX_QUALITY) {
            throw new IllegalArgumentException("quality must be finite and between -1 and 1");
        }
        if (!Float.isFinite(qualityAdjustment) || Math.abs(qualityAdjustment) > ItemQualityAdjustment.MAX_ADJUSTMENT) {
            throw new IllegalArgumentException("qualityAdjustment must be finite and between -2 and 2");
        }
    }

    /** Creates a request without quality adjustment. */
    public ItemRequest(ItemTemplate template, Integer modelId, String name, Integer hue, int amount,
                       Direction direction, ItemMaterial material, float quality) {
        this(template, modelId, name, hue, amount, direction, material, quality, 0.0f);
    }

    /** Creates a request with neutral quality. */
    public ItemRequest(ItemTemplate template, Integer modelId, String name, Integer hue, int amount,
                       Direction direction, ItemMaterial material) {
        this(template, modelId, name, hue, amount, direction, material, 0.0f);
    }

    /** Creates a request that inherits the template material. */
    public ItemRequest(ItemTemplate template, Integer modelId, String name, Integer hue, int amount,
                       Direction direction) {
        this(template, modelId, name, hue, amount, direction, null);
    }

    public static ItemRequest byTemplate(ItemTemplate template) {
        return new ItemRequest(template, null, null, null, 1, null, null);
    }

    public static ItemRequest byModelId(int modelId) {
        return new ItemRequest(null, modelId, null, null, 1, null, null);
    }

    public static ItemRequest byName(String name) {
        return new ItemRequest(null, null, name, null, 1, null, null);
    }

    /**
     * Requests the named template as an item derived from {@code source}, copying its hue, material and quality.
     * Since the material and its quality travel together, the derived item keeps the source material until a
     * quality adjustment is applied. The source hue is copied as is, so a source without color produces an item
     * without color. A source without material inherits the template material.
     * Amount and direction keep the defaults of {@link #byName(String)}.
     *
     * @param source item whose hue, material and quality are copied; it is not modified
     * @param name template name of the item to create
     */
    public static ItemRequest derivedFrom(UOItem source, String name) {
        Objects.requireNonNull(source, "source");
        return byName(name)
                .withHue(source.getHue())
                .withMaterial(source.getMaterial())
                .withQuality(source.getQuality());
    }

    public ItemRequest withHue(int hue) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality, qualityAdjustment);
    }

    public ItemRequest withAmount(int amount) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality, qualityAdjustment);
    }

    public ItemRequest withDirection(Direction direction) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality, qualityAdjustment);
    }

    /** Replaces the template material on the created item; {@code null} inherits the template material again. */
    public ItemRequest withMaterial(ItemMaterial material) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality, qualityAdjustment);
    }

    /** Declares the quality the resolved material already reflects; it is stored without changing the material. */
    public ItemRequest withQuality(float quality) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality, qualityAdjustment);
    }

    /**
     * Adds {@code adjustment} to the quality adjustment applied on creation. Positive values raise the quality,
     * negative values lower it, and zero keeps the material unchanged.
     */
    public ItemRequest adjustQuality(float adjustment) {
        return new ItemRequest(template, modelId, name, hue, amount, direction, material, quality,
                qualityAdjustment + adjustment);
    }
}
