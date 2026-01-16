package com.github.mayconr.juoserver.game;

import java.util.List;

import com.github.mayconr.juoserver.game.core.session.ChannelGroupSessionFanout;
import com.github.mayconr.juoserver.game.core.session.SessionFanout;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import com.github.mayconr.juoserver.game.core.ai.BankerAI;
import com.github.mayconr.juoserver.game.core.ai.DefaultNpcAiRegistry;
import com.github.mayconr.juoserver.game.core.ai.NpcAiRegistry;
import com.github.mayconr.juoserver.game.core.ai.ollama.OllamaClientChatImpl;
import com.github.mayconr.juoserver.game.core.ai.ollama.OllanaClient;
import com.github.mayconr.juoserver.game.core.combat.CombatSystem;
import com.github.mayconr.juoserver.game.core.combat.DefaultCombatSystem;
import com.github.mayconr.juoserver.game.core.event.DefaultEventBus;
import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.gameloop.DefaultGameLoop;
import com.github.mayconr.juoserver.game.core.gameloop.GameLoop;
import com.github.mayconr.juoserver.game.core.gump.DefaultHandlerGumpSystem;
import com.github.mayconr.juoserver.game.core.gump.GumpSystem;
import com.github.mayconr.juoserver.game.core.gump.GumpSystemCallback;
import com.github.mayconr.juoserver.game.core.prototype.PrototypeConfiguration;
import com.github.mayconr.juoserver.game.core.session.game.DefaultGameSession;
import com.github.mayconr.juoserver.game.core.session.game.GameSession;
import com.github.mayconr.juoserver.game.core.session.game.ItemService;
import com.github.mayconr.juoserver.game.core.session.game.MessageService;
import com.github.mayconr.juoserver.game.core.session.npc.NpcSessionFactory;
import com.github.mayconr.juoserver.game.core.session.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.packet.handler.*;
import com.github.mayconr.juoserver.game.server.ClientConnectedHandlerAdapter;
import com.github.mayconr.juoserver.game.server.ServerStartup;
import com.github.mayconr.juoserver.game.server.UOChannelInitializer;
import com.github.mayconr.juoserver.game.storage.DatabaseConfiguration;
import com.github.mayconr.juoserver.game.storage.WorldService;
import com.github.mayconr.juoserver.game.storage.account.AccountStorage;
import com.github.mayconr.juoserver.game.storage.mobile.MobileStorage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

@Configuration
@Import({DatabaseConfiguration.class, PrototypeConfiguration.class})
public class ApplicationConfiguration {

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
    public GumpSystem gumpSystem(ChannelGroup channelGroup) {
        return new DefaultHandlerGumpSystem(channelGroup);
    }

    // ========= Session Factory / Core Game Session =========

    @Bean
    public PlayerSessionFactory playerSessionFactory(
            ChannelGroup channelGroup,
            EventBus eventBus,
            WorldService worldService,
            GameLoop gameLoop,
            CombatSystem combatSystem) {
        return new PlayerSessionFactory(
                channelGroup, eventBus, worldService, gameLoop, combatSystem);
    }

    @Bean
    public GameSession gameSession(
            WorldService worldService,
            ChannelGroup channelGroup,
            SessionFanout fanout,
            PlayerSessionFactory playerSessionFactory,
            NpcSessionFactory npcSessionFactory,
            EventBus eventBus) {
        final var messageService = new MessageService(channelGroup);
        final var itemService = new ItemService(worldService, channelGroup, eventBus);
        return new DefaultGameSession(
                worldService,
                channelGroup,
                fanout,
                eventBus,
                playerSessionFactory,
                npcSessionFactory,
                messageService,
                itemService);
    }

    // ========= Packet Handlers =========

    @Bean
    public List<SimpleChannelInboundHandler<?>> packetHandlers(
            AccountStorage accountStorage,
            MobileStorage mobileStorage,
            WorldService worldService,
            GameSession gameSession,
            GumpSystemCallback gumpSystemCallback) {
        return List.of(
                new GameServerLoginHandler(accountStorage, mobileStorage, worldService),
                new PingPongHandler(),
                new LoginCharacterHandler(gameSession, worldService),
                new DeleteCharacterHandler(worldService),
                new CreateCharacterHandler(worldService, gameSession, mobileStorage),
                new ClientVersionHandler(gameSession),
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
        return new ChannelGroupSessionFanout(channelGroup);
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
            WorldService worldService, OllanaClient ollanaClient, EventBus eventBus) {
        final var registry = new DefaultNpcAiRegistry();
        registry.registerAI("BANKER", () -> new BankerAI(worldService, ollanaClient, eventBus));
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
}
