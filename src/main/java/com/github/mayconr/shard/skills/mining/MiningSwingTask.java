package com.github.mayconr.shard.skills.mining;

import com.github.mayconr.juoserver.game.gameloop.GameTask;
import com.github.mayconr.juoserver.game.model.AnimationOptions;
import com.github.mayconr.juoserver.game.model.AnimationType;
import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.world.WorldActions;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import lombok.RequiredArgsConstructor;

import java.util.Collections;

@RequiredArgsConstructor
public class MiningSwingTask implements GameTask {
    private static final int SWING_INTERVAL_TICKS = 20; // ex: 1s se tick=100ms

    private final WorldActions worldActions;
    private final SkillSystem skillSystem;
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

        worldActions.sendAnimation(player, AnimationOptions.simpleForward(AnimationType.SWING_SWORD_FROM_HORSE, 10));

        final var item = worldActions.createItemInContainer("iron_ore", player);

        skillSystem.tryGain(player, 49, 100, new SkillGainContext(player, Collections.emptyMap()));

        System.out.println(player.getSkills().get(49));

        // TODO create item in backpack
        // TODO support to stack item
    }

    @Override
    public boolean isDone() {
        return swingsLeft <= 0;
    }
}
