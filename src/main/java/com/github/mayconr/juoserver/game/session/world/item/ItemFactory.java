package com.github.mayconr.juoserver.game.session.world.item;

import com.github.mayconr.juoserver.common.template.ItemTemplate;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOItem;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;

import java.util.UUID;

public class ItemFactory {

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
                template.type(),
                template.layer(),
                0,
                template.hue(),
                template.movable(),
                false,
                Direction.NORTH,
                null,
                template.mountNpc()
        );
        if (template.attr().containsKey("gumpId")) {
            return new UOContainer(item, (int) template.attr().get("gumpId"));
        }
        return item;
    }
}
