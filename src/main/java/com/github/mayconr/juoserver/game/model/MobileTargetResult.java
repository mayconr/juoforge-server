package com.github.mayconr.juoserver.game.model;

public record MobileTargetResult(UOPlayer source, Location location, UOMobile mobile) implements TargetResult {
}
