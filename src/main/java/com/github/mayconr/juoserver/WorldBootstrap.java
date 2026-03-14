package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.item.template.CachedItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.mobile.npc.template.CachedNpcTemplateRegistry;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.world.DefaultWorld;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyRegistry;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystemImpl;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.rng.DefaultRNG;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;

import java.nio.file.Path;

public final class WorldBootstrap {

    private final JuoforgeConfiguration config;

    public WorldBootstrap(JuoforgeConfiguration config) {
        this.config = config;
    }

    public World start() {
        // --- Core infra
        EventBus eventBus = new DefaultEventBus();
        RNG rng = new DefaultRNG();

        // --- Policy
        PolicyRegistry policyRegistry = new PolicyRegistry();
        PolicyService policyService = new PolicyService(policyRegistry);

        // --- Item use
        ItemUseRegistry itemUseRegistry = new ItemUseRegistry();
        ItemUseService itemUseService = new ItemUseService(itemUseRegistry);

        // --- Templates

        NpcTemplateRegistry npcTemplateRegistry =
                new CachedNpcTemplateRegistry(new JsonTemplateLoader<>(Path.of("template/npcs"), NpcTemplate.class).load());

        ItemTemplateRegistry itemTemplateRegistry =
                new CachedItemTemplateRegistry(new JsonTemplateLoader<>(Path.of("template/items"), ItemTemplate.class).load());

        // --- Region
        RegionSystem regionSystem = new RegionSystemImpl(new JsonTemplateLoader<>(Path.of("template/regions"), RegionTemplate.class));

        MobileStorage mobileStorage = config.world().mobileStorage();
        ItemStorage itemStorage = config.world().itemStorage();
        RealmStorage realmStorage = new CachedRealmStorage(mobileStorage, itemStorage);

        // --- Serial
        SerialGenerator serialGenerator = new SerialGenerator(realmStorage);

        // --- Game loop (ciclo de vida explícito)
        DefaultGameLoop gameLoop = new DefaultGameLoop(config);
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        gameLoop.start();

        // --- World
        var fileReaderSystem = new UOFileReaderSystem(config);

        DefaultWorld world = new DefaultWorld(
                eventBus,
                serialGenerator,
                realmStorage,
                gameLoop,
                regionSystem,
                fileReaderSystem,
                policyService,
                itemUseService,
                rng,

                // Templates
                itemTemplateRegistry,
                npcTemplateRegistry,

                config
        );

        world.initialize();

        gameLoop.addTask(new GameTask() {
            @Override public void execute(long currentTick, double delta) { world.update(delta); }
            @Override public boolean isDone() { return false; }
        });

        return world;
    }
}
