package com.github.mayconr.juoserver.game.core.database;

import org.springframework.context.annotation.Bean;

import com.github.mayconr.juoserver.game.core.prototype.PrototypeManager;

public class DatabaseConfiguration {

    @Bean
    public Database database(PrototypeManager prototypeManager) {
        return new HardcodedDatabase(prototypeManager);
    }
}
