package com.github.mayconr.juoserver.infrastructure.storage.item;

import com.github.mayconr.juoserver.common.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOItem;

import java.util.UUID;

public class ItemFactory {

    public static UOItem createFromTemplate(ItemTemplate template, Location location) {
        return new UOItem(
                UUID.randomUUID(),
                -1,
                template.modelId(),
                location.getX(),
                location.getY(),
                location.getZ(),
                template.name(),
                template.type(),
                template.layer(),
                0,
                template.hue(),
                template.movable(),
                false,
                Direction.NORTH,
                null,
                null
        );
    }
}
