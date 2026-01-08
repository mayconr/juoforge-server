package com.github.mayconr.juoserver.game.core.prototype;

import org.springframework.context.annotation.Bean;

public class PrototypeConfiguration {

    @Bean
    public PrototypeManager prototype() {
        return new PrototypeManagerImpl();
    }
}
