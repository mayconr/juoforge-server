package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.TargetType;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.game.world.WorldView;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UseMiningTool implements ItemUseTrigger {

    private final WorldActions worldActions;
    private final WorldView worldView;
    private final ResourceRoller resourceRoller;

    @Override
    public boolean supports(ItemUseContext ctx) {
        return ctx.item().getName().equals("pickaxe");
    }

    @Override
    public void execute(ItemUseContext ctx) {
        final var player = ctx.player();
        final var initialLocation = new PointInTheWorld(player);

        if (!ctx.player().isItemEquipped(ctx.item())) {
            worldActions.sendMessage(player, new PlainTextMessageContent("Pickaxe must be eqquiped"));
        }

        worldActions.sendMessage(player, new PlainTextMessageContent("Select a region to mining!"));
        worldActions.sendTarget(player, CursorType.NEUTRAL, result-> handleMining(initialLocation, result));
    }

    private void handleMining(Location initialLocation, TargetResult result) {
        final var player = result.sender();

        // Target was a static
        if (!TargetType.STATICS.equals(result.type())) {
            return;
        }

        final var mapTile = worldView.getLandTile(result.location());

        // is landTile mineralizable
        if (!MineableLandTile.isMineable(mapTile.id())) {
            worldActions.sendMessage(player, new PlainTextMessageContent("Location cannot be mining!"));
            return;
        }

        // check if player moved from his initial position

        // Add mining task
        worldActions.scheduleTask(new MiningSwingTask(worldActions, resourceRoller, player));
    }

}
