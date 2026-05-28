package com.github.mayconr.juoserver.game.model;

public record DeathRequest(
        UOMobile victim,
        UOObject<?> killer,
        DeathCause cause
) {

    public static DeathRequest of(UOMobile victim, UOObject<?> killer, DeathCause cause) {
        return new DeathRequest(victim, killer, cause);
    }

}
