package com.github.mayconr.juoserver.game.model;

import java.util.List;

public record PlayerDetails(UOAccount account,
                            String password,
                            String name,
                            Status status,
                            Location location,
                            List<UOItem> equippedItems,
                            List<SkillValue> skills) {

    public record Status(int strength, int dexterity, int intelligence) {

    }


}

