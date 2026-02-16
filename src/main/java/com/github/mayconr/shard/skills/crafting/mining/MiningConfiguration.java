package com.github.mayconr.shard.skills.crafting.mining;

import com.github.mayconr.juoserver.game.world.WorldActions;
import com.github.mayconr.juoserver.game.world.WorldView;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MiningConfiguration {

    @Bean
    public UseMiningTool useMiningTool(WorldActions worldActions, WorldView worldView, ResourceRoller resourceRoller) {
        return new UseMiningTool(worldActions, worldView, resourceRoller);
    }
}
