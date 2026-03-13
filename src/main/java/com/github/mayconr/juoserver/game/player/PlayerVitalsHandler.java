package com.github.mayconr.juoserver.game.player;

import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerVitalsHandler {

    private final WorldInternal world;

    public void update(UOPlayer player, double delta) {
        double regenAccumulator = player.getRuntimeAttribute("REGEN_ACCUMULATOR", 0d) + delta;

        if (regenAccumulator > 10) {
            world.regen(player, delta);
            regenAccumulator -= 10;
        }

        player.setRuntimeAttribute("REGEN_ACCUMULATOR", regenAccumulator);
    }

}
