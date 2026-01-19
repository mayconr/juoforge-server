package com.github.mayconr.juoserver.game.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Layer {
    /** Invalid layer. */
    INVALID(0x00),
    /** First valid layer. Equivalent to OneHanded. */
    FIRST_VALID(0x01),
    /** One handed weapon. */
    ONE_HANDED(0x01),
    /** Two handed weapon or shield. */
    TWO_HANDED(0x02),
    /** Shoes. */
    SHOES(0x03),
    /** Pants. */
    PANTS(0x04),
    /** Shirts. */
    SHIRT(0x05),
    /** Helmets), hats), and masks. */
    HEAD(0x06),
    /** Gloves. */
    GLOVES(0x07),
    /** Rings. */
    RING(0x08),
    /** Talismans. */
    TALISMAN(0x09),
    /** Gorgets and necklaces. */
    NECK(0x0A),
    /** Hair. */
    HAIR(0x0B),
    /** Half aprons. */
    WAIST(0x0C),
    /** Torso, inner layer. */
    INNER_TORSO(0x0D),
    /** Bracelets. */
    BRACELET(0x0E),
    /** Unused. */
    UNUSED_XF(0x0F),
    /** Beards and mustaches. */
    FACIAL_HAIR(0x10),
    /** Torso), outer layer. */
    MIDDLE_TORSO(0x11),
    /** Earrings. */
    EARRINGS(0x12),
    /** Arms and sleeves. */
    ARMS(0x13),
    /** Cloaks. */
    CLOAK(0x14),
    /** Backpacks. */
    BACKPACK(0x15),
    /** Torso, outer layer. */
    OUTER_TORSO(0x16),
    /** Leggings, outer layer. */
    OUTER_LEGS(0x17),
    /** Leggings, inner layer. */
    INNER_LEGS(0x18),
    /** Last valid non-internal layer. Equivalent to <c>Layer.InnerLegs</c>. */
    LAST_USER_VALID(0x18),
    /** Mount item layer. */
    MOUNT(0x19),
    /** Vendor 'buy pack' layer. */
    SHOP_BUY(0x1A),
    /** Vendor 'resale pack' layer. */
    SHOP_RESALE(0x1B),
    /** Vendor 'sell pack' layer. */
    SHOP_SELL(0x1C),
    /** Bank box layer. */
    BANK(0x1D),
    /** Last valid layer. Equivalent to <tt>Layer.Bank</tt>. */
    LAST_VALID(0x1D);

    private final int code;

    public static Layer fromCode(int code) {
        for (Layer layer : values()) {
            if (layer.code == code) {
                return layer;
            }
        }
        return Layer.INVALID;
    }
}
