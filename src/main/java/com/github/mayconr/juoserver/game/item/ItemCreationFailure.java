package com.github.mayconr.juoserver.game.item;

import java.util.Arrays;
import java.util.Optional;

/**
 * Game-rule reasons that reject an item creation. Failure codes outside this set are
 * programming or infrastructure errors and are raised as exceptions instead.
 */
public enum ItemCreationFailure {
    TEMPLATE_NOT_FOUND("ITEM_TEMPLATE_NOT_FOUND", "{item.template_not_found}"),
    NOT_STACKABLE("ITEM_NOT_STACKABLE", "{item.not_stackable}"),
    BACKPACK_OVERWEIGHT("BACKPACK_OVERWEIGHT", "{item.backpack_overweight}");

    private final String code;
    private final String displayMessage;

    ItemCreationFailure(String code, String displayMessage) {
        this.code = code;
        this.displayMessage = displayMessage;
    }

    /** Flow failure code that identifies this reason. */
    public String code() {
        return code;
    }

    /** Player-facing text or localization key. */
    public String displayMessage() {
        return displayMessage;
    }

    /** Maps a flow failure code to a rejection reason; empty when the code is not a game-rule rejection. */
    public static Optional<ItemCreationFailure> fromCode(String code) {
        return Arrays.stream(values())
                .filter(failure -> failure.code.equals(code))
                .findFirst();
    }
}
