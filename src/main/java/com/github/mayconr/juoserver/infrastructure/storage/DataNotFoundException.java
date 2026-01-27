package com.github.mayconr.juoserver.infrastructure.storage;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }

    public DataNotFoundException() {
    }
}
