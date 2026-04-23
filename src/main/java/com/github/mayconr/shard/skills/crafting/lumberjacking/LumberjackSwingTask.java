package com.github.mayconr.shard.skills.crafting.lumberjacking;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.shard.skills.Skills;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LumberjackSwingTask implements GameTask {

    private final UOPlayer player;
    private final WorldActions worldActions;
    private final ResourceRoller resourceRoller;

    private static final int SWING_INTERVAL_TICKS = 30;
    private long nextTick = 0;
    private int swingsLeft = 5;

    @Override
    public void execute(long currentTick, double delta) {
        if (currentTick < nextTick) {
            return;
        }
        swingsLeft--;
        nextTick = currentTick + SWING_INTERVAL_TICKS;

        AnimationOptions options;
        if (player.isMounted()) {
            options = AnimationOptions.simpleForward(AnimationType.SWING_SWORD_FROM_HORSE, 20);
        } else {
            options = AnimationOptions.simpleForward(AnimationType.ATTACK_WITH_SWORD_SIDE, 20);
        }
        worldActions.sendAnimation(player, options);

        worldActions.tryGainSkill(player, Skills.LUMBERJACK.getId(), 100, SkillGainContext.of(player));

        var item = resourceRoller.rollResource(50);

        if (item != null) {
            UOContainer container = null;//
            final var woodItem = worldActions.createItem(ItemRequest.byName(item.toString().toLowerCase()), ItemTarget.container(container));
            if (log.isDebugEnabled()) {
                log.debug("Wood [{}] created in [{}-{}] backpackItem", woodItem.getName(), player.getSerialId(), player.getName());
            }
        }
    }

    @Override
    public boolean isDone() {
        return swingsLeft <= 0;
    }
}
