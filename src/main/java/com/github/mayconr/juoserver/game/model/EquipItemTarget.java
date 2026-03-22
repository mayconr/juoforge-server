package com.github.mayconr.juoserver.game.model;

public record EquipItemTarget(UOMobile mobile) implements ItemTarget {

    public static EquipItemTarget of(UOMobile mobile) {
        return new EquipItemTarget(mobile);
    }

}
