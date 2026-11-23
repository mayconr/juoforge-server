package com.github.mayconr.juoserver.game.container;

/** Controls stack quantities, nested traversal and cache warmup from storage. */
public record CountItemsOptions(
        boolean countStackAmount,
        boolean searchNestedContainers,
        boolean warmup
) {}
