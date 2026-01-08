package com.github.mayconr.juoserver.game.core.model;

public class NpcPrototypeNotFoundException extends RuntimeException {
    public NpcPrototypeNotFoundException(String name) {
        super("Prototype not found for name [" + name + "]");
    }
}
