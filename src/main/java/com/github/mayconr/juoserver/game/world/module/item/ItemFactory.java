package com.github.mayconr.juoserver.game.world.module.item;

import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.template.definitions.item.ItemTemplate;

import java.util.Optional;
import java.util.UUID;

public class ItemFactory {

    public static UOItem createFromTemplate(SerialGenerator serialGenerator, ItemTemplate template) {
        return ItemFactory.createFromTemplate(serialGenerator, template, new PointInTheWorld(0,0,0));
    }

    public static UOItem createFromTemplate(SerialGenerator serialGenerator, ItemTemplate template, Location location) {
        final var item = new UOItem(
                UUID.randomUUID(),
                serialGenerator.nextItemSerial(),
                template.modelId(),
                location.getX(),
                location.getY(),
                location.getZ(),
                template.name(),
                template.displayName(),
                template.attr(),
                template.layer(),
                0,
                template.hue(),
                template.movable(),
                false,
                Direction.NORTH,
                null,
                template.flags()
        );
        if (template.flags().contains(ItemFlag.CONTAINER)) {
            return new UOContainer(item, Optional.ofNullable(template.attr().get("gumpId"))
                    .map(Integer.class::cast).orElse(0));
        }
        return item;
    }
}
