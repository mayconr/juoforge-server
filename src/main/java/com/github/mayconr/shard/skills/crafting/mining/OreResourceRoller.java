package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import com.github.mayconr.shard.skills.crafting.DefaultResourceRoller;

import java.util.List;

public class OreResourceRoller extends DefaultResourceRoller<Ore> {

    private List<Ore> ores;

    public OreResourceRoller(RNG rng, TemplateRegistry<String, Ore> templateRegistry) {
        super(rng, List.copyOf(templateRegistry.all()));
    }
}
