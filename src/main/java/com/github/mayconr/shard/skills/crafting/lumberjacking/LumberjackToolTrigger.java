package com.github.mayconr.shard.skills.crafting.lumberjacking;

import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.Static;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.game.world.WorldView;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LumberjackToolTrigger implements ItemUseTrigger {

    private final WorldActions worldActions;
    private final WorldView worldView;
    private final ResourceRoller resourceRoller;

    @Override
    public boolean supports(ItemUseContext ctx) {
        return ctx.item().getName().equals("axe");
    }

    @Override
    public void execute(ItemUseContext ctx) {
        final var initialLocation = new PointInTheWorld(ctx.player());
        worldActions.sendTarget(ctx.player(), CursorType.NEUTRAL, result -> handleLumberjack(initialLocation, result));
    }

    private void handleLumberjack(Location initialLocation, TargetResult result) {
        final var player = result.source();

        if (!result.isStatic()) {
            return;
        }

        final var statics = worldView.getStatics(result.location());
        if (!canLumberjack(statics)) {
            return;
        }

        worldActions.scheduleTask(new LumberjackSwingTask(player, worldActions, resourceRoller));
    }

    private boolean canLumberjack(List<Static> statics) {
        return Arrays.stream(LogableStatic.values())
                .anyMatch(type-> statics.stream().anyMatch(sta->sta.id() > type.getMin() && sta.id() < type.getMax()));
    }
}
