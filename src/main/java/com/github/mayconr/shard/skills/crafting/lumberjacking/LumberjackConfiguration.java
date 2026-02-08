package com.github.mayconr.shard.skills.crafting.lumberjacking;

import com.github.mayconr.juoserver.game.session.world.WorldActions;
import com.github.mayconr.juoserver.game.session.world.WorldView;
import com.github.mayconr.shard.skills.crafting.ResourceRoller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LumberjackConfiguration {

    @Bean
    public UseLumberjackTool useLumberjackTool(WorldActions worldActions, WorldView worldView, ResourceRoller resourceRoller) {
        return new UseLumberjackTool(worldActions, worldView, resourceRoller);
    }

}
