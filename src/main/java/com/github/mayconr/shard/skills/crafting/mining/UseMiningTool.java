package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.trigger.item.ItemUseContext;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseTrigger;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetType;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import com.github.mayconr.juoserver.game.session.world.WorldView;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UseMiningTool implements ItemUseTrigger {

    private final WorldActions worldActions;
    private final WorldView worldView;
    private final SkillSystem skillSystem;
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
            worldActions.sendMessage(player, "Pickaxe must be eqquiped", MessageOptions.standard());
        }

        worldActions.sendMessage(player, "Select a region to mining!", MessageOptions.standard());
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
            worldActions.sendMessage(player, "Location cannot be mining!", MessageOptions.standard());
            return;
        }

        // check if player moved from his initial position

        // Add mining task
        worldActions.scheduleTask(new MiningSwingTask(worldActions, skillSystem, resourceRoller, player));
    }

}
