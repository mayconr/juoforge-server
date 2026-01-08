package com.github.mayconr.juoserver.game.core.model;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(int serialId) {
        super("Item serial [" + serialId + " not found]");
    }
}
