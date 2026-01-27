package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.common.event.DefaultEventBus;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.policy.PolicyRegistry;
import com.github.mayconr.juoserver.common.policy.PolicyService;
import com.github.mayconr.juoserver.common.template.HardcodedItemRegistry;
import com.github.mayconr.juoserver.common.template.HardcodedNpcTemplateLoader;
import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.common.template.NpcTemplateRegistry;
import com.github.mayconr.juoserver.common.useitem.ItemUseRegistry;
import com.github.mayconr.juoserver.common.useitem.ItemUseService;
import com.github.mayconr.juoserver.game.ai.BankerAI;
import com.github.mayconr.juoserver.game.ai.DefaultNpcAiRegistry;
import com.github.mayconr.juoserver.game.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.game.ai.ollama.OllamaClientChatImpl;
import com.github.mayconr.juoserver.game.ai.ollama.OllanaClient;
import com.github.mayconr.juoserver.game.combat.CombatSystem;
import com.github.mayconr.juoserver.game.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.game.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.gump.DefaultGumpSystem;
import com.github.mayconr.juoserver.game.gump.GumpSystem;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.session.world.DefaultWorldSession;
import com.github.mayconr.juoserver.game.session.world.MessageService;
import com.github.mayconr.juoserver.game.session.world.SerialGenerator;
import com.github.mayconr.juoserver.game.session.world.WorldSession;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerMobileService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import io.netty.channel.group.ChannelGroup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
    public GameLoop gameLoop() {
        final var gameLoop = new DefaultGameLoop();
        Runtime.getRuntime().addShutdownHook(new Thread(gameLoop::stop));
        return gameLoop.start();
    }

    @Bean
    public OllanaClient ollanaClient() {
        return new OllamaClientChatImpl("http://localhost:11434");
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

    // =========================================================================
    // AI
    // =========================================================================

    @Bean
    public NpcAiRegistry npcAiRegistry(
            RealmStorage realmStorage,
            OllanaClient ollanaClient,
            EventBus eventBus) {

        final var registry = new DefaultNpcAiRegistry();
        registry.registerAI(
                "BANKER",
                () -> new BankerAI(realmStorage, ollanaClient, eventBus)
        );
        return registry;
    }

    @Bean
    public NpcSessionFactory npcSessionFactory(
            EventBus eventBus,
            ChannelGroup channelGroup,
            GameLoop gameLoop,
            NpcAiRegistry aiRegistry) {

        return new NpcSessionFactory(eventBus, channelGroup, gameLoop, aiRegistry);
    }

    // =========================================================================
    // Templates
    // =========================================================================

    @Bean
    public NpcTemplateRegistry npcTemplateRegistry() {
        return new HardcodedNpcTemplateLoader();
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry() {
        return new HardcodedItemRegistry();
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
            ItemUseService itemUseService) {

        return new PlayerSessionFactory(
                eventBus,
                gameLoop,
                combatSystem,
                policyService,
                serverProperties,
                itemUseService
        );
    }

    @Bean
    public WorldSession worldSession(
            RealmStorage realmStorage,
            ChannelGroup channelGroup,
            SessionFanout fanout,
            PlayerSessionFactory playerSessionFactory,
            NpcSessionFactory npcSessionFactory,
            EventBus eventBus,
            ItemTemplateRegistry itemTemplateRegistry,
            NpcTemplateRegistry npcTemplateRegistry) {

        final var serialGenerator = new SerialGenerator(realmStorage);
        final var messageService = new MessageService(channelGroup);
        final var itemService = new ItemService(
                serialGenerator,
                itemTemplateRegistry,
                realmStorage,
                fanout,
                eventBus
        );
        final var playerMobileService = new PlayerMobileService(
                serialGenerator,
                realmStorage,
                itemTemplateRegistry
        );
        final var playerSessionService = new PlayerSessionService(
                playerSessionFactory,
                eventBus,
                fanout
        );

        final var worldSession = new DefaultWorldSession(
                serialGenerator,
                realmStorage,
                fanout,
                eventBus,
                playerSessionFactory,
                npcSessionFactory,
                npcTemplateRegistry,
                messageService,
                itemService,
                playerMobileService,
                playerSessionService
        );

        worldSession.initialize();
        return worldSession;
    }
}
