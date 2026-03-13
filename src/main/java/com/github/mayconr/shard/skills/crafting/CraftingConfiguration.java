package com.github.mayconr.shard.skills.crafting;

import com.github.mayconr.juoserver.game.world.World;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CraftingConfiguration {

    @Bean
    public ResourceRoller resourceRoller(World world) {
        return new DefaultResourceRoller(world);
    }

}
