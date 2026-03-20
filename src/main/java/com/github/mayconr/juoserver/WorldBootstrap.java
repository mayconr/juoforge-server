package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.DefaultWorldCfg.TemplateData;
import com.github.mayconr.juoserver.game.GamePlaySettings;
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
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderImpl;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyRegistry;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystem;
import com.github.mayconr.juoserver.infrastructure.region.RegionSystemImpl;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.rng.DefaultRNG;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.*;
import com.github.mayconr.juoserver.infrastructure.template.InMemoryTemplateRegistry;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import com.github.mayconr.juoserver.infrastructure.template.TemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public final class WorldBootstrap {

    public static final String GAMEPLAY_CONFIG = "GAMEPLAY_CONFIG_BY_NAME";
    private final ShardBootstrap shardBootstrap;

    public ServerRuntime start() {
        final var configuration = new DefaultWorldCfg();

        configuration.addCustomTemplate(GAMEPLAY_CONFIG, GamePlaySettings.class, GamePlaySettings::name, Path.of("template/config/gameplay.json"));

        // Initial configuration
        shardBootstrap.configure(configuration);

        // --- Templates
        final Map<String, TemplateRegistry> registryMap = new HashMap<>();
        for (TemplateData data : configuration.templateList()) {
            var templates = data.templateLoader().loadAll();
            registryMap.put(data.templateName(), new InMemoryTemplateRegistry<>(templates, data.keyExtractor()));
        }

        // Core Templates
        final GamePlaySettings settings = (GamePlaySettings) registryMap.get(GAMEPLAY_CONFIG).get("DEFAULT").getFirst();

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

        MobileStorage mobileStorage = configuration.mobileStorage();
        ItemStorage itemStorage = configuration.itemStorage();
        AccountStorage accountStorage = configuration.accountStorage();
        RealmStorage storage = new CachedRealmStorage(mobileStorage, itemStorage, accountStorage);

        // --- Serial
        SerialGenerator serialGenerator = new SerialGenerator(storage);

        // --- Game loop (ciclo de vida explícito)
        DefaultGameLoop gameLoop = new DefaultGameLoop(settings);
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        gameLoop.start();

        // --- World
        var uoFileReader = new UOFileReaderImpl(settings);

        DefaultWorld world = new DefaultWorld(
                eventBus,
                serialGenerator,
                storage,
                gameLoop,
                regionSystem,
                uoFileReader,
                policyService,
                itemUseService,
                rng,

                // Templates
                itemTemplateRegistry,
                npcTemplateRegistry,

                settings,
                configuration
        );

        world.initialize();

        gameLoop.addTask(new GameTask() {
            @Override public void execute(long currentTick, double delta) { world.update(delta); }
            @Override public boolean isDone() { return false; }
        });

        final var runtime = new InternalServerRuntime(world, registryMap, settings, eventBus, storage, uoFileReader, gameLoop);
        for (var factory : configuration.itemTriggerList()) {
            itemUseRegistry.register(factory.apply(runtime));
        }
        for (var factory : configuration.eventListenerList()) {
            eventBus.register(factory.apply(runtime));
        }

        return runtime;
    }

    private record InternalServerRuntime(World world,
                                         Map<String, TemplateRegistry> registryMap,
                                         GamePlaySettings settings,
                                         EventBus eventBus,
                                         RealmStorage storage,
                                         UOFileReader fileReader,
                                         GameLoop gameLoop) implements ServerRuntime {

            @Override
            public <K, V> TemplateRegistry<K, V> getTemplateRegistry(String templateName, Class<V> clazz) {
                return registryMap.get(templateName);
            }
        }

}
