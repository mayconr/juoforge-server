package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;

public interface ServerRuntime {

    World world();

    <K,V> TemplateRegistry<K, V> getTemplateRegistry(String templateName, Class<V> clazz);

    GamePlaySettings settings();

    EventBus eventBus();

    RealmStorage storage();

    UOFileReader fileReader();

    GameLoop gameLoop();
}
