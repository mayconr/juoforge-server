package com.github.mayconr.shard.command;

import com.github.mayconr.juoserver.game.model.ItemTargetResult;
import com.github.mayconr.juoserver.game.model.MobileTargetResult;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.UOContainer;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.Prompt;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
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
        if (event.arguments().length == 0 || event.arguments()[0].isBlank()) {
            sendHelp(event.player());
            return;
        }
        var target = event.arguments().length > 1 ? event.arguments()[1] : "ground";
        var template = event.arguments()[0];
        var player = event.player();

        switch (target) {
            case "equipped" -> sendCursor(player, result -> createEquippedItem(result, template));
            case "bag" -> sendCursor(player, result -> createContainerItem(result, template));
            case "ground" -> sendCursor(player, result->createGroundItem(result, template));
            default -> sendHelp(player);
        }
    }

    private void sendHelp(UOPlayer player) {
        world.message().send(player, "Uso: .createitem <template> [ground|bag|equipped]");
        world.message().send(player, "ground: cria no local selecionado (padrao); bag: cria no container selecionado; equipped: equipa no mobile selecionado.");
        world.message().send(player, "Exemplo: .createitem pickaxe ground");
    }

    private void sendCursor(UOPlayer player, Consumer<TargetResult> resultConsumer) {
        world.interaction().sendTarget(player, CursorType.NEUTRAL, result -> {
            try {
                resultConsumer.accept(result);
            } catch (ModuleOperationException error) {
                world.message().send(player, error.getDisplayMessage());
            }
        });
    }

    private void createEquippedItem(TargetResult result, String template) {
        if (result instanceof MobileTargetResult rs) {
            world.item().createItem(ItemRequest.byName(template), ItemTarget.equip(rs.mobile()));
        }
    }

    private void createGroundItem(TargetResult result, String template) {
        world.item().createItem(ItemRequest.byName(template), ItemTarget.dropAt(result.location()));
    }

    private void createContainerItem(TargetResult result, String template) {
        if (result instanceof ItemTargetResult rs) {
            world.item().createItem(ItemRequest.byName(template), ItemTarget.container((UOContainer) rs.item()));
        }
    }
}
