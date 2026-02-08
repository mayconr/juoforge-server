package com.github.mayconr.shard.skills.crafting;

import com.github.mayconr.juoserver.game.rng.RNG;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class DefaultResourceRoller implements ResourceRoller {

    private final RNG rng;

    @Override
    public <T extends CraftingResource> T rollResource(T[] resources, double miningSkill) {
        double successChance = Math.min(miningSkill, 100.0) / 100.0;

        if (rng.roll(successChance)) {
            return null;
        }

        // available ores pool
        var possibleOres = Arrays.stream(resources)
                .filter(resource -> miningSkill >= resource.getMinSkill())
                .toList();

        if (possibleOres.isEmpty()) {
            return null;
        }

        // choose an ore
        return possibleOres.get(ThreadLocalRandom.current().nextInt(possibleOres.size()));
    }
}
