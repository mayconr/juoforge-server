package com.github.mayconr.juoserver.infrastructure.storage.serial;

import java.util.concurrent.CompletableFuture;

/** Serial allocation for new items and mobiles. */
public interface Serials {

    CompletableFuture<Integer> nextItem();

    CompletableFuture<Integer> nextMobile();
}
