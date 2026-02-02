package com.github.mayconr.shard.skills.mining;

import com.github.mayconr.juoserver.common.useitem.ItemUseContext;
import com.github.mayconr.juoserver.common.useitem.ItemUseTrigger;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.session.player.target.TargetResult;
import com.github.mayconr.juoserver.game.session.player.target.TargetType;
import com.github.mayconr.juoserver.game.session.world.*;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UseMiningTool implements ItemUseTrigger {

    private final WorldActions worldActions;
    private final WorldView worldView;
    private final GameLoop gameLoop;
    private final SkillSystem skillSystem;

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
        worldActions.sendTarget(player, CursorType.NEUTRAL, result->locationSelected(player, initialLocation, result));
    }

    private void locationSelected(UOPlayer player, Location initialLocation, TargetResult result) {

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
        gameLoop.addTask(new MiningSwingTask(worldActions, skillSystem, player));
    }

}
