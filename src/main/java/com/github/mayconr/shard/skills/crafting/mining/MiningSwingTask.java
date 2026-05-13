package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.*;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.game.model.event.message.PlainTextMessageContent;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.shard.skills.Skills;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class MiningSwingTask implements GameTask {
    private static final int SWING_INTERVAL_TICKS = 30; // ex: 1s se tick=100ms

    private final World world;
    private final Function<Double, Ore> resourceRoller;
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
        world.sendAnimation(player, options);
        world.tryGainSkill(player, Skills.MINING.getId(), 100, SkillGainContext.of(player));

        final var skill = player.getSkills().get(Skills.MINING.getId());
        final var ore = resourceRoller.apply(skill.getValue());

        if (ore == null) {
            world.sendMessage(player, new PlainTextMessageContent("You loosen some rocks but find nothing of value."));
            return;
        }
        final var itemRequest = ItemRequest.byName(ore.itemName())
                .withAmount(2);
        UOContainer container = null;
        final var oreItem = world.createItem(itemRequest, ContainerItemTarget.of(container, cfg->{
            cfg.tryStack(true);
        }));

        world.sendMessage(player, MessageContent.plain("you finished "+oreItem.getAmount()));

        if (log.isDebugEnabled()) {
            log.debug("Ore [{}] created in [{}-{}] backpackItem", oreItem.getName(), player.getSerialId(), player.getName());
        }
    }

    @Override
    public boolean isDone() {
        return swingsLeft <= 0;
    }

    @Override
    public void onDone(long currentTick, double delta) {
        world.sendMessage(player, new PlainTextMessageContent("You are done of mining"));
    }
}
