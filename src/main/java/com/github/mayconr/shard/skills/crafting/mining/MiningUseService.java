package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.ServerRuntime;
import com.github.mayconr.juoserver.game.interaction.target.TargetResult;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MiningUseService {

    private final ResourceRoller<Ore> resourceRoller;
    private final MiningTargetValidator targetValidator;
    private final World world;

    public MiningUseService(ResourceRoller<Ore> resourceRoller, ServerRuntime runtime, MiningTargetValidator targetValidator) {
        this.resourceRoller = resourceRoller;
        this.targetValidator = targetValidator;
        this.world = runtime.world();
    }

    public void start(ItemUseContext ctx) {
        final var player = ctx.player();

        if (!player.isItemEquipped(ctx.item())) {
            world.sendMessage(player, new PlainTextMessageContent("Pickaxe must be equipped"));
            return;
        }

        final var initialLocation = new PointInTheWorld(player);

        world.sendMessage(player, new PlainTextMessageContent("Select a region to mine!"));
        world.sendTarget(
                player,
                CursorType.NEUTRAL,
                targetResult -> handleTarget(player, initialLocation, targetResult)
        );
    }

    private void handleTarget(
            UOPlayer player,
            Location initialLocation,
            TargetResult targetResult) {
        final var validation = targetValidator.validate(world, initialLocation, targetResult);

        if (!validation.isValid()) {
            world.sendMessage(player, new PlainTextMessageContent(validation.message()));
            return;
        }

        world.scheduleTask(new MiningSwingTask(world, resourceRoller::rollResource, player));
    }

}
