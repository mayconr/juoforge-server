package com.github.mayconr.juoserver.game.core.model;

public class UOCity {
    private final String name;
    private final String location;
    private final PointInTheWorld startingLocation;

    public UOCity(String name, String location, PointInTheWorld startingLocation) {
        this.name = name;
        this.location = location;
        this.startingLocation = startingLocation;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public PointInTheWorld getStartingLocation() {
        return startingLocation;
    }
}
