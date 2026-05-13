package com.github.mayconr.juoserver.game.model;

public record DeathRequest(
        UOMobile victim,
        UOObject killer,
        DeathCause cause
) {}
