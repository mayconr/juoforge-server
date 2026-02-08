package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.ai.ollama.OllamaClientChatImpl;
import com.github.mayconr.juoserver.game.ai.ollama.OllanaClient;
import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.event.DefaultEventBus;
import com.github.mayconr.juoserver.game.event.EventBus;
import com.github.mayconr.juoserver.game.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.model.event.MobileMoved;
import com.github.mayconr.juoserver.game.policy.PolicyRegistry;
import com.github.mayconr.juoserver.game.policy.PolicyService;
import com.github.mayconr.juoserver.game.reader.UOFileReader;
import com.github.mayconr.juoserver.game.rng.DefaultRNG;
import com.github.mayconr.juoserver.game.rng.RNG;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.session.world.DefaultWorld;
import com.github.mayconr.juoserver.game.session.world.MessageService;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.game.session.world.WorldInternal;
import com.github.mayconr.juoserver.game.session.world.animation.AnimationService;
import com.github.mayconr.juoserver.game.session.world.item.EquipItemService;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.session.world.movement.MovementService;
import com.github.mayconr.juoserver.game.session.world.movement.RangeDetection;
import com.github.mayconr.juoserver.game.session.world.npc.NpcService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerCreationService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerRemovalService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.game.session.world.skill.SkillService;
import com.github.mayconr.juoserver.game.session.world.speech.SpeechService;
import com.github.mayconr.juoserver.game.skill.DefaultSkillSystem;
import com.github.mayconr.juoserver.game.skill.SkillSystem;
import com.github.mayconr.juoserver.game.template.*;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseRegistry;
import com.github.mayconr.juoserver.game.trigger.item.ItemUseService;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import io.netty.channel.group.ChannelGroup;
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
    public OllanaClient ollanaClient() {
        return new OllamaClientChatImpl("http://localhost:11434");
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
    // Combat / Systems
    // =========================================================================

    @Bean
    public CombatSystem combatSystem(GameLoop gameLoop, ChannelGroup channelGroup) {
        final var combatSystem = new DefaultCombatSystem(channelGroup);
        gameLoop.addTask(combatSystem);
        return combatSystem;
    }

    @Bean
    public GumpSystem gumpSystem(SessionFanout fanout) {
        return new DefaultGumpSystem(fanout);
    }

    @Bean
    public SkillSystem skillSystem(ServerProperties properties, RNG rng) {
        return new DefaultSkillSystem(properties, rng);
    }

    // =========================================================================
    // AI
    // =========================================================================

    @Bean
    public NpcSessionFactory npcSessionFactory(
            EventBus eventBus,
            SessionFanout fanout) {

        return new NpcSessionFactory(eventBus, fanout);
    }

    // =========================================================================
    // Templates
    // =========================================================================

    @Bean
    public TemplateLoader<NpcTemplate> npcTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/npcs"), NpcTemplate.class);
    }

    @Bean
    public NpcTemplateRegistry npcTemplateRegistry(TemplateLoader<NpcTemplate> templateLoader) {
        return new DefaultNpcTemplateRegistry(templateLoader.load());
    }

    @Bean
    public TemplateLoader<ItemTemplate> itemTemplateLoader() {
        return new JsonTemplateLoader<>(Path.of("template/items"), ItemTemplate.class);
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry(TemplateLoader<ItemTemplate> itemTemplateLoader) {
        return new DefaultItemTemplateRegistry(itemTemplateLoader.load());
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
    public PlayerSessionFactory playerSessionFactory(
            EventBus eventBus,
            GameLoop gameLoop,
            CombatSystem combatSystem,
            PolicyService policyService,
            ServerProperties serverProperties,
            ItemUseService itemUseService,
            RealmStorage storage) {

        return new PlayerSessionFactory(
                eventBus,
                gameLoop,
                combatSystem,
                policyService,
                serverProperties,
                itemUseService,
                storage
        );
    }

    @Bean
    public WorldInternal worldSession(
            RealmStorage storage,
            ChannelGroup channelGroup,
            SessionFanout fanout,
            PlayerSessionFactory playerSessionFactory,
            EventBus eventBus,
            ItemTemplateRegistry itemTemplateRegistry,
            GameLoop gameLoop,
            SkillSystem skillSystem,
            ServerProperties properties,
            PolicyService policyService,
            NpcTemplateRegistry npcTemplateRegistry,
            NpcSessionFactory npcSessionFactory) {

        final var serialGenerator = new SerialGenerator(storage);
        final var messageService = new MessageService(channelGroup);
        final var playerSessionService = new PlayerSessionService(
                playerSessionFactory,
                eventBus,
                fanout
        );
        final var itemService = new ItemService(
                serialGenerator,
                itemTemplateRegistry,
                storage,
                fanout,
                eventBus,
                playerSessionService
        );
        final var playerMobileService = new PlayerCreationService(
                serialGenerator,
                storage,
                itemTemplateRegistry,
                properties,
                policyService
        );
        final var fileReader = new UOFileReader();
        final var animationService = new AnimationService(fanout);
        final var npcService = new NpcService(
                npcSessionFactory,
                npcTemplateRegistry,
                itemService,
                serialGenerator,
                storage,
                eventBus);
        final var movementService = new MovementService(eventBus, storage);
        final var speechService = new SpeechService(eventBus);
        final var equipItemService = new EquipItemService(storage, eventBus);
        final var playerRemovalService = new PlayerRemovalService(storage, eventBus);
        final var skillService = new SkillService(eventBus, storage);

        final var world = new DefaultWorld(
                serialGenerator,
                storage,
                fanout,
                gameLoop,
                skillSystem,
                playerSessionFactory,
                messageService,
                itemService,
                playerMobileService,
                playerSessionService,
                fileReader,
                animationService,
                npcService,
                movementService,
                speechService,
                equipItemService,
                playerRemovalService,
                skillService
        );

        eventBus.register(MobileMoved.class, new RangeDetection(world, eventBus, properties));
        world.initialize();
        return world;
    }
}
