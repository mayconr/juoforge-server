package com.github.mayconr.juoserver.game.core.model;

public class MobileNotFoundException extends RuntimeException {
    public MobileNotFoundException(int serialId) {
        super("Mobile serial [" + serialId + "] not found.");
    }
}
