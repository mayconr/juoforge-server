package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class CreateItem extends AbstractCommand {

    private final World world;

    public CreateItem(World world) {
        super("createitem");
        this.world = world;
    }

    @Override
    public void handle(Prompt event) {
        var target = event.arguments().length > 1 ? event.arguments()[1] : "ground";
        var template = event.arguments()[0];
        var player = event.player();

        switch (target) {
            case "equipped" -> sendCursor(player, result -> createEquippedItem(result, template));
            case "bag" -> sendCursor(player, result -> createContainerItem(result, template));
            case "ground" -> sendCursor(player, result->createGroundItem(result, template));
        }
    }

    private void sendCursor(UOPlayer player, Consumer<TargetResult> resultConsumer) {
        world.sendTarget(player, CursorType.NEUTRAL, resultConsumer);
    }

    private void createEquippedItem(TargetResult result, String template) {
        world.getMobileBySerialId(result.serialId())
            .ifPresent(mobile -> world.createItem(ItemRequest.byName(template), ItemTarget.equip(mobile)));
    }

    private void createGroundItem(TargetResult result, String template) {
        world.createItem(ItemRequest.byName(template), ItemTarget.dropAt(result.location()));
    }

    private void createContainerItem(TargetResult result, String template) {
        world.getContainerBySerialId(result.serialId())
                .ifPresent(container -> world.createItem(ItemRequest.byName(template), ItemTarget.container(container)));
    }
}
