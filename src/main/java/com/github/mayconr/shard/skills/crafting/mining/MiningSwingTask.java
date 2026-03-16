package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.item.ItemCreationRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.ItemOptions.ContainerTarget;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.shard.skills.Skills;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MiningSwingTask implements GameTask {
    private static final int SWING_INTERVAL_TICKS = 30; // ex: 1s se tick=100ms

    private final WorldActions worldActions;
    private final ResourceRoller resourceRoller;
    private final UOPlayer player;
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
        worldActions.tryGainSkill(player, Skills.MINING.getId(), 100, SkillGainContext.of(player));

        final var skill = player.getSkills().get(Skills.MINING.getId());
        final var ore = resourceRoller.rollResource(OreType.values(), skill.getValue());

        if (ore == null) {
            worldActions.sendMessage(player, new PlainTextMessageContent("You loosen some rocks but find nothing of value."));
            return;
        }
        final var oreItem = worldActions.createItem(ItemCreationRequest.byName(ore.name().toLowerCase()).build(), ItemOptions.builder().target(new ContainerTarget(player)).build());

        if (log.isDebugEnabled()) {
            log.debug("Ore [{}] created in [{}-{}] backpack", oreItem.getName(), player.getSerialId(), player.getName());
        }
    }

    @Override
    public boolean isDone() {
        return swingsLeft <= 0;
    }

    @Override
    public void onDone(long currentTick, double delta) {
        worldActions.sendMessage(player, new PlainTextMessageContent("You are done of mining"));
    }
}
