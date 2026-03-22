package com.github.mayconr.shard.skills.crafting;

import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class DefaultResourceRoller<T extends CraftingResource> implements ResourceRoller<T> {

    private final RNG rng;
    private final List<T> resources;

    @Override
    public T rollResource(double baseSkill) {
        double successChance = Math.min(baseSkill, 100.0) / 100.0;

        if (rng.roll(successChance)) {
            return null;
        }

        final List<T> possibleResources = new ArrayList<>();
        for (T resource : resources) {
            if (baseSkill >= resource.minSkill()) {
                possibleResources.add(resource);
            }
        }

        if (possibleResources.isEmpty()) {
            return null;
        }

        // choose an ore
        return possibleResources.get(ThreadLocalRandom.current().nextInt(possibleResources.size()));
    }
}
