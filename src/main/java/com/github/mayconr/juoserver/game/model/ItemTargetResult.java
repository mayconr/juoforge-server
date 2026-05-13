package com.github.mayconr.juoserver.game.model;

public record ItemTargetResult(UOPlayer source, Location location, UOItem item) implements TargetResult {

}
