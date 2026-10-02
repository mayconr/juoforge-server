package com.github.mayconr.shard.skills.crafting.lumberjacking;

import com.github.mayconr.juoforge.reader.view.StaticTile;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseContext;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseTrigger;
import com.github.mayconr.juoserver.game.model.CursorType;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.TargetResult;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.item.WorldItem;
import com.github.mayconr.juoserver.game.skill.WorldSkill;
import com.github.mayconr.juoserver.game.interaction.WorldInteraction;
import com.github.mayconr.juoserver.game.map.WorldMap;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class LumberjackToolTrigger implements ItemUseTrigger {

    private final GameLoop gameLoop;
    private final WorldItem items;
    private final WorldSkill skills;
    private final WorldInteraction interaction;
    private final WorldMap map;
    private final ResourceRoller resourceRoller;

    @Override
    public boolean supports(ItemUseContext ctx) {
        return ctx.item().getName().equals("axe");
    }

    @Override
    public void execute(ItemUseContext ctx) {
        final var initialLocation = new PointInTheWorld(ctx.player());
        interaction.sendTarget(ctx.player(), CursorType.NEUTRAL, result -> handleLumberjack(initialLocation, result));
    }

    private void handleLumberjack(Location initialLocation, TargetResult result) {
        final var player = result.source();

        if (!result.isStatic()) {
            return;
        }

        final var statics = map.getStatics(result.location());
        if (!canLumberjack(statics)) {
            return;
        }

        gameLoop.addTask(new LumberjackSwingTask(player, items, skills, interaction, resourceRoller));
    }

    private boolean canLumberjack(List<StaticTile> statics) {
        return Arrays.stream(LogableStatic.values())
                .anyMatch(type-> statics.stream().anyMatch(sta->sta.id() > type.getMin() && sta.id() < type.getMax()));
    }
}
