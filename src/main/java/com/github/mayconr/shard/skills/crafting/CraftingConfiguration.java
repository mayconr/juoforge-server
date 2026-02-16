package com.github.mayconr.shard.skills.crafting;

import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CraftingConfiguration {

    @Bean
    public ResourceRoller resourceRoller(RNG rng) {
        return new DefaultResourceRoller(rng);
    }

}
