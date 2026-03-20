package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOAccount;
import com.github.mayconr.juoserver.game.player.template.BodyTemplate;

import java.util.List;

public record PlayerDetails(UOAccount account,
                            String name,
                            int skinColor,
                            BodyTemplate bodyTemplate,
                            Status status,
                            Location location,
                            List<ItemRequest> startkit,
                            List<SkillValue> skills) {

    public record Status(int strength, int dexterity, int intelligence) { }

}

