package com.github.mayconr.juoserver.game.core.model;

public class ItemPrototypeNotFoundException extends RuntimeException {
    public ItemPrototypeNotFoundException(int itemId) {
        super("Item prototype id [" + itemId + "] not found");
    }

    public ItemPrototypeNotFoundException(String name) {
        super("Item prototype name [" + name + "] not found");
    }
}
