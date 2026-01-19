package com.github.mayconr.juoserver.game.model;

public class ContainerNotFoundException extends RuntimeException {
    public ContainerNotFoundException(int serialId) {
        super("Container [" + serialId + "] not found");
    }
}
