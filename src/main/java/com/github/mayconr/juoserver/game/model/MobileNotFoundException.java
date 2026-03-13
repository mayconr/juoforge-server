package com.github.mayconr.juoserver.game.model;

public class MobileNotFoundException extends RuntimeException {
    public MobileNotFoundException(int serialId) {
        super("Mobile serial [" + serialId + "] not found.");
    }
}
