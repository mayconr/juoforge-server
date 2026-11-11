package com.github.mayconr.juoserver.game.item;

import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

/** Outcome of an item creation: the created item or the game rule that rejected it. */
public sealed interface ItemCreationResult {

    /** Returns the created item or raises the rejection as a {@link ModuleOperationException}. */
    UOItem orThrow();

    record Created(UOItem item) implements ItemCreationResult {

        @Override
        public UOItem orThrow() {
            return item;
        }
    }

    /**
     * @param reason game rule that rejected the creation
     * @param detail technical description for logs
     */
    record Rejected(ItemCreationFailure reason, String detail) implements ItemCreationResult {

        /** Player-facing text or localization key. */
        public String displayMessage() {
            return reason.displayMessage();
        }

        @Override
        public UOItem orThrow() {
            throw new ModuleOperationException("createItem", reason.code(), detail, displayMessage(), null);
        }
    }
}
