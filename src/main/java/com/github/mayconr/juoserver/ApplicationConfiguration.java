package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.common.event.DefaultEventBus;
import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.policy.ActionPolicyRegistry;
import com.github.mayconr.juoserver.common.policy.ActionPolicyService;
import com.github.mayconr.juoserver.common.template.HardcodedItemRegistry;
import com.github.mayconr.juoserver.common.template.HardcodedNpcTemplateLoader;
import com.github.mayconr.juoserver.common.template.ItemTemplateRegistry;
import com.github.mayconr.juoserver.common.template.NpcTemplateRegistry;
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
import com.github.mayconr.juoserver.game.gump.GumpSystemCallback;
import com.github.mayconr.juoserver.game.session.DefaultSessionFanout;
import com.github.mayconr.juoserver.game.session.SessionFanout;
import com.github.mayconr.juoserver.game.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.session.world.*;
import com.github.mayconr.juoserver.game.session.world.item.ItemService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerMobileService;
import com.github.mayconr.juoserver.game.session.world.player.PlayerSessionService;
import com.github.mayconr.juoserver.infrastructure.server.ClientConnectedHandlerAdapter;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.infrastructure.server.UOChannelInitializer;
import com.github.mayconr.juoserver.infrastructure.storage.CachedRealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.ItemStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.network.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;

@Configuration
public class ApplicationConfiguration {
    // ==== Policy ===

    @Bean
    public ActionPolicyRegistry actionPolicyRegistry() {
        return new ActionPolicyRegistry();
    }

    @Bean
    public ActionPolicyService actionPolicyService(ActionPolicyRegistry registry) {
        return new ActionPolicyService(registry);
    }

    // ========= Independents =========

    @Bean
    public OllanaClient ollanaClient() {
        return new OllamaClientChatImpl("http://localhost:11434");
    }

    @Bean
    public EventBus eventBus() {
        return new DefaultEventBus();
    }

    @Bean
    public GameLoop gameLoop() {
        final var gameloop = new DefaultGameLoop();
        Runtime.getRuntime().addShutdownHook(new Thread(gameloop::stop));
        return gameloop.start();
    }

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

    // ========= Session Factory / Core Game Session =========

    @Bean
    public PlayerSessionFactory playerSessionFactory(
            EventBus eventBus,
            GameLoop gameLoop,
            CombatSystem combatSystem,
            ActionPolicyService policyService) {
        return new PlayerSessionFactory(eventBus, gameLoop, combatSystem, policyService);
    }

    @Bean
    public WorldSession gameSession(
            RealmStorage storage,
            ChannelGroup channelGroup,
            SessionFanout fanout,
            PlayerSessionFactory playerSessionFactory,
            NpcSessionFactory npcSessionFactory,
            EventBus eventBus,
            ItemTemplateRegistry itemTemplateRegistry,
            NpcTemplateRegistry npcTemplateRegistry) {
        final var serialGenerator = new SerialGenerator(storage);
        final var messageService = new MessageService(channelGroup);
        final var itemService = new ItemService(serialGenerator, itemTemplateRegistry, storage, fanout, eventBus);
        final var playerMobileService = new PlayerMobileService(serialGenerator, storage, itemTemplateRegistry);
        final var playerSessionService = new PlayerSessionService(playerSessionFactory, eventBus, fanout);

        final var gameSession = new DefaultWorldSession(
                serialGenerator,
                storage,
                fanout,
                eventBus,
                playerSessionFactory,
                npcSessionFactory,
                npcTemplateRegistry,
                messageService,
                itemService,
                playerMobileService,
                playerSessionService);
        gameSession.initialize();
        return gameSession;
    }

    // ========= Packet Handlers =========

    @Bean
    public List<SimpleChannelInboundHandler<?>> packetHandlers(
            AccountStorage accountStorage,
            MobileStorage mobileStorage,
            RealmStorage worldStorage,
            WorldSession worldSession,
            GumpSystemCallback gumpSystemCallback) {
        return List.of(
                new GameServerLoginHandler(accountStorage, mobileStorage, worldStorage),
                new PingPongHandler(),
                new LoginCharacterHandler(worldSession, worldStorage),
                new DeleteCharacterHandler(worldStorage),
                new CreateCharacterHandler(worldStorage, worldSession, mobileStorage),
                new ClientVersionHandler(worldSession),
                new MoveRequestHandler(),
                new DoubleClickHandler(),
                new UnicodeSpeachRequestHandler(),
                new MegaClilocHandler(),
                new GeneralInformationHandler(),
                new LookRequestHandler(),
                new PickUpItemHandler(),
                new DropItemHandler(),
                new WearItemHandler(),
                new TargetHandler(),
                new GetPlayerStatusHandler(),
                new RequestHelpHandler(),
                new RequestWarModeHandler(),
                new AttackRequestHandler(),
                new GumpSelectionHandler(gumpSystemCallback));
    }

    // ========= Network =========
    @Bean
    public ChannelGroup channelGroup() {
        return new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Bean
    public SessionFanout sessionFanout(ChannelGroup channelGroup) {
        return new DefaultSessionFanout(channelGroup);
    }

    @Qualifier("parent")
    @Bean
    public NioEventLoopGroup parentNioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Qualifier("child")
    @Bean
    public NioEventLoopGroup childNioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    public ClientConnectedHandlerAdapter connectionLoggingHandler(ChannelGroup channelGroup) {
        return new ClientConnectedHandlerAdapter(channelGroup);
    }

    @DependsOn({"connectionLoggingHandler", "packetHandlers"})
    @Bean
    public UOChannelInitializer channelInitializer(
            ClientConnectedHandlerAdapter clientConnectedHandlerAdapter,
            List<SimpleChannelInboundHandler<?>> packetHandlers) {
        return new UOChannelInitializer(clientConnectedHandlerAdapter, packetHandlers);
    }

    @Bean
    public ServerBootstrap serverBootstrap(
            UOChannelInitializer channelInitializer,
            @Qualifier("parent") NioEventLoopGroup parentNioEventLoopGroup,
            @Qualifier("child") NioEventLoopGroup childNioEventLoopGroup) {
        return new ServerBootstrap()
                .group(parentNioEventLoopGroup, childNioEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Bean
    public ServerStartup serverStartup(
            ServerBootstrap serverBootstrap,
            @Qualifier("parent") NioEventLoopGroup parentNioEventLoopGroup,
            @Qualifier("child") NioEventLoopGroup childNioEventLoopGroup) {
        return new ServerStartup(serverBootstrap, parentNioEventLoopGroup, childNioEventLoopGroup);
    }

    // ========= AI =========

    @Bean
    public NpcAiRegistry npcAiRegistry(
            RealmStorage worldStorage, OllanaClient ollanaClient, EventBus eventBus) {
        final var registry = new DefaultNpcAiRegistry();
        registry.registerAI("BANKER", () -> new BankerAI(worldStorage, ollanaClient, eventBus));
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

    // Template
    @Bean
    public NpcTemplateRegistry npcTemplateRegistry() {
        return new HardcodedNpcTemplateLoader();
    }

    @Bean
    public ItemTemplateRegistry itemTemplateRegistry() {
        return new HardcodedItemRegistry();
    }

    // Realm Storage
    @Bean
    public RealmStorage realmStorage(MobileStorage mobileStorage, ItemStorage itemStorage) {
        return new CachedRealmStorage(mobileStorage, itemStorage);
    }
}
