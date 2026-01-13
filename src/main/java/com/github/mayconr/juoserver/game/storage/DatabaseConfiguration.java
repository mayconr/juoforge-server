package com.github.mayconr.juoserver.game.storage;

import org.springframework.context.annotation.Bean;

import com.github.mayconr.juoserver.game.core.prototype.PrototypeManager;
import com.github.mayconr.juoserver.game.storage.item.ItemStorage;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;

public class DatabaseConfiguration {

    @Bean
    public WorldService database(
            PrototypeManager prototypeManager,
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {
        return new CachedWorldService(prototypeManager, mobileStorage, itemStorage);
    }
}
