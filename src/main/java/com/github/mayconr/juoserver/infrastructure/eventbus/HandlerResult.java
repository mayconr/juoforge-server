package com.github.mayconr.juoserver.infrastructure.eventbus;

public enum HandlerResult {
    CONTINUE, // Continue processing next handlers
    BLOCK // Stop further processing
}
