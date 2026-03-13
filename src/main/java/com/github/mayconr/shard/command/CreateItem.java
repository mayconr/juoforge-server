package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.model.ItemOptions;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.WorldActions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreateItem extends AbstractCommand {
    private final WorldActions worldActions;

    public CreateItem(WorldActions worldActions) {
        super("createitem");
        this.worldActions = worldActions;
    }

    @Override
    public void handle(Prompt event) {
        worldActions.createItem(ItemCreationRequest.byName(event.arguments()[0]).build(),
                ItemOptions.builder()
                        .target(new ItemOptions.ContainerTarget(event.player()))
                        .build());
    }
}
