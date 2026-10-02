package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;

public class MiningUseService {

    private final ResourceRoller<Ore> resourceRoller;
    private final MiningTargetValidator targetValidator;
    private final World world;
    private final GameLoop gameLoop;

    public MiningUseService(ResourceRoller<Ore> resourceRoller, World world, GameLoop gameLoop, MiningTargetValidator targetValidator) {
        this.resourceRoller = resourceRoller;
        this.targetValidator = targetValidator;
        this.world = world;
        this.gameLoop = gameLoop;
    }

    public void start(ItemUseContext ctx) {
        final var player = ctx.player();

        if (!player.isItemEquipped(ctx.item())) {
            world.message().send(player, new PlainTextMessageContent("Pickaxe must be equipped"));
            return;
        }

        final var initialLocation = new PointInTheWorld(player);

        world.message().send(player, new PlainTextMessageContent("Select a region to mine!"));
        world.interaction().sendTarget(
                player,
                CursorType.NEUTRAL,
                targetResult -> handleTarget(player, initialLocation, targetResult)
        );
    }

    private void handleTarget(
            UOPlayer player,
            Location initialLocation,
            TargetResult targetResult) {
        final var validation = targetValidator.validate(world.map(), initialLocation, targetResult);

        if (!validation.isValid()) {
            world.message().send(player, new PlainTextMessageContent(validation.message()));
            return;
        }

        gameLoop.addTask(new MiningSwingTask(world, resourceRoller::rollResource, player));
    }

}
