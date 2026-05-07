package com.github.mayconr.juoserver.game.model;

public sealed interface TargetResult permits MobileTargetResult, ItemTargetResult, StaticTargetResult {
    UOPlayer source();
    Location location();

    default boolean isMobile() {
        return this instanceof MobileTargetResult;
    }

    default boolean isItem() {
        return this instanceof ItemTargetResult;
    }

    default boolean isStatic() {
        return this instanceof StaticTargetResult;
    }
}
