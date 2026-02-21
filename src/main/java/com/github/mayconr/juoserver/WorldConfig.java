package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.economy.template.RegionStockTemplate;
import com.github.mayconr.juoserver.game.item.template.CachedItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.template.ItemTemplate;
import com.github.mayconr.juoserver.game.item.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseRegistry;
import com.github.mayconr.juoserver.game.item.trigger.ItemUseService;
import com.github.mayconr.juoserver.game.mobile.npc.template.CachedNpcTemplateRegistry;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplate;
import com.github.mayconr.juoserver.game.mobile.npc.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.game.player.DefaultSessionManager;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionManager;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillHandler;
import com.github.mayconr.juoserver.game.skill.SkillModule;
import com.github.mayconr.juoserver.game.world.DefaultWorld;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.game.world.World;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.game.world.bootstrap.ShardConfiguration;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReaderSystem;
import com.github.mayconr.juoserver.infrastructure.eventbus.DefaultEventBus;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameLoop;
import com.github.mayconr.juoserver.infrastructure.gameloop.GameTask;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyRegistry;
import com.github.mayconr.juoserver.infrastructure.policy.PolicyService;
import com.github.mayconr.juoserver.infrastructure.region.MapRegionSystem;
import com.github.mayconr.juoserver.infrastructure.region.MapRegionSystemImpl;
import com.github.mayconr.juoserver.infrastructure.region.RegionTemplate;
import com.github.mayconr.juoserver.infrastructure.rng.DefaultRNG;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.template.JsonTemplateLoader;
import com.github.mayconr.juoserver.infrastructure.template.TemplateLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;

@EnableConfigurationProperties(ServerProperties.class)
public class WorldConfig {

    // =========================================================================
    // Core Infrastructure
    // =========================================================================

    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    public GameLoop gameLoop(ServerProperties properties) {
        final var gameLoop = new DefaultGameLoop(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        return gameLoop.start();
    }

    @Bean
    public RNG rng() {
        return new DefaultRNG();
    }

    // =========================================================================
    // Policy
    // =========================================================================

    @Bean
    public PolicyRegistry policyRegistry() {
        return new PolicyRegistry();
    }

    @Bean
    public PolicyService policyService(PolicyRegistry registry) {
        return new PolicyService(registry);
    }

    // =========================================================================
    // Item Use
    // =========================================================================

    @Bean
    public ItemUseRegistry itemUseRegistry() {
        return new ItemUseRegistry();
    }

    @Bean
    public ItemUseService itemUseService(ItemUseRegistry registry) {
        return new ItemUseService(registry);
    }

    // =========================================================================
    // Templates
    // =========================================================================

    @Bean
    public TemplateLoader<NpcTemplate> npcTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/npcs"), NpcTemplate.class);
    }

    @Bean
    public TemplateLoader<RegionTemplate> regionTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/regions"), RegionTemplate.class);
    }

    @Bean
    public TemplateLoader<ItemTemplate> itemTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/items"), ItemTemplate.class);
    }

    @Bean
    public NpcTemplateRegistry npcTemplateRegistry(TemplateLoader<NpcTemplate> templateLoader) {
        return new CachedNpcTemplateRegistry(templateLoader.load());
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry(TemplateLoader<ItemTemplate> itemTemplateLoader) {
        return new CachedItemTemplateRegistry(itemTemplateLoader.load());
    }

    // =========================================================================
    // Region
    // =========================================================================

    @Bean
    public MapRegionSystem mapRegionService(TemplateLoader<RegionTemplate> templateLoader) {
        return new MapRegionSystemImpl(templateLoader);
    }

    // =========================================================================
    // Storage
    // =========================================================================

    @Bean
    public RealmStorage realmStorage(
            MobileStorage mobileStorage,
            ItemStorage itemStorage) {

        return new CachedRealmStorage(mobileStorage, itemStorage);
    }

    // =========================================================================
    // Sessions / World
    // =========================================================================

    @Bean
    public SerialGenerator serialGenerator(RealmStorage realmStorage) {
        return new SerialGenerator(realmStorage);
    }

    @Bean
    public SessionManager sessionRegistry(WorldInternal world, EventBus eventBus) {
        return new DefaultSessionManager(world, eventBus);
    }

    @Bean
    public PlayerSessionFactory playerSessionFactory(ServerProperties properties) {
        return new PlayerSessionFactory(properties);
    }

    @Bean
    public SkillModule skillModule(ServerProperties properties, RNG rng, EventBus eventBus, RealmStorage storage) {
        final var skillSystem = new DefaultSkillSystem(properties, rng, eventBus);
        final var skillHandler = new SkillHandler(eventBus, storage);
        return  new SkillModule(skillHandler, skillSystem);
    }

    @Bean
    public World world(
            RealmStorage storage,
            EventBus eventBus,
            SerialGenerator serialGenerator,
            ItemTemplateRegistry itemTemplateRegistry,
            NpcTemplateRegistry npcTemplateRegistry,
            GameLoop gameLoop,
            ServerProperties properties,
            PolicyService policyService,
            ItemUseService itemUseService,
            MapRegionSystem regionSystem,
            RNG rng,
            ShardConfiguration configuration) {

        final var fileReaderSystem = new UOFileReaderSystem(properties);

        final var world = new DefaultWorld(
                eventBus,
                serialGenerator,
                storage,
                gameLoop,
                regionSystem,
                itemTemplateRegistry,
                fileReaderSystem,
                policyService,
                itemUseService,
                rng,
                npcTemplateRegistry,
                configuration,
                properties
        );

        world.initialize();

        gameLoop.addTask(new GameTask() {
            @Override
            public void execute(long currentTick, double delta) {
                world.update(delta);
            }

            @Override
            public boolean isDone() {
                return false;
            }
        });

        return world;
    }
}
