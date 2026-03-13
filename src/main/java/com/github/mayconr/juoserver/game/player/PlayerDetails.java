package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOAccount;

import java.util.List;

public record PlayerDetails(UOAccount account,
                            String name,
                            Status status,
                            Location location,
                            List<ItemCreationRequest> startkit,
                            List<SkillValue> skills) {

    public record Status(int strength, int dexterity, int intelligence) {

    }

    public record ItemInfo(String name, int modelId, int hue, int amount) {}

}

