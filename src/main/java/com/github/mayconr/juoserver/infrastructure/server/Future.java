package com.github.mayconr.juoserver.infrastructure.server;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public final class Future {

    public static void fire(CompletableFuture<?> future) {
        future.whenComplete((v, ex)->{
            if (ex != null) {
                log.error("Unhandled async error", ex);
            }
        });
    }

}
