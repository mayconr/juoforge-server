package com.github.mayconr.juoserver.game.shard;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gump.GumpSystem;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.shard.commands.Goto;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShardConfiguration {

    @Bean
    public ApplicationRunner configure(EventBus bus, GumpSystem gumpSystem, GameSession gameSession) {
        return args-> {
            bus.register(new Goto(gumpSystem, gameSession));
        };
    }

}
