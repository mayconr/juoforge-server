package com.github.mayconr.juoserver.game.model;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(int serialId) {
        super("Item serial [" + serialId + " not found]");
    }
}
