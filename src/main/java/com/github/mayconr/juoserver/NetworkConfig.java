package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.player.DefaultSessionFanout;
import com.github.mayconr.juoserver.game.player.PlayerSessionFactory;
import com.github.mayconr.juoserver.game.player.SessionFanout;
import com.github.mayconr.juoserver.game.player.SessionManager;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.server.ClientConnectedHandlerAdapter;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.infrastructure.server.UOChannelInitializer;
import com.github.mayconr.juoserver.infrastructure.storage.AccountStorage;
import com.github.mayconr.juoserver.infrastructure.storage.MobileStorage;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
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
import org.springframework.context.annotation.DependsOn;

import java.util.List;

public class NetworkConfig {
    // =========================================================================
    // Network Core
    // =========================================================================

    @Bean
    public ChannelGroup channelGroup() {
        return new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    @Bean
    public SessionFanout sessionFanout(ChannelGroup channelGroup) {
        return new DefaultSessionFanout(channelGroup);
    }

    @Bean
    @Qualifier("parent")
    public NioEventLoopGroup parentNioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    @Qualifier("child")
    public NioEventLoopGroup childNioEventLoopGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    public ClientConnectedHandlerAdapter connectionLoggingHandler(
            ChannelGroup channelGroup) {

        return new ClientConnectedHandlerAdapter(channelGroup);
    }

    // =========================================================================
    // Packet Handlers
    // =========================================================================

    @Bean
    public List<SimpleChannelInboundHandler<?>> packetHandlers(
            AccountStorage accountStorage,
            MobileStorage mobileStorage,
            RealmStorage realmStorage,
            WorldInternal world,
            SessionManager sessionManager,
            PlayerSessionFactory playerSessionFactory,
            SessionFanout fanout) {

        return List.of(
                new GameServerLoginHandler(accountStorage, mobileStorage, realmStorage),
                new PingPongHandler(),
                new LoginCharacterHandler(world, playerSessionFactory, sessionManager, fanout),
                new DeleteCharacterHandler(realmStorage),
                new CreatePlayerHandler(world, playerSessionFactory, fanout, sessionManager),
                new ClientVersionHandler(world),
                new MoveRequestHandler(world),
                new DoubleClickHandler(world),
                new UnicodeSpeachRequestHandler(world),
                new TooltipRequestHandler(world),
                new GeneralInformationHandler(),
                new SingleClickHandler(world),
                new ItemUnequippedHandler(world),
                new DropItemHandler(world),
                new EquipItemHandler(world),
                new TargetHandler(world),
                new GetPlayerStatusHandler(world),
                new RequestHelpHandler(world),
                new RequestWarModeHandler(world),
                new AttackRequestHandler(world),
                new GumpSelectionHandler(world),
                new UseRequestHandler(world),
                new ActionRequestedHandler(world),
                new SendSkillHandler(world)
        );
    }

    // =========================================================================
    // Bootstrap
    // =========================================================================

    @Bean
    @DependsOn({"connectionLoggingHandler", "packetHandlers"})
    public UOChannelInitializer channelInitializer(
            ClientConnectedHandlerAdapter clientConnectedHandlerAdapter,
            List<SimpleChannelInboundHandler<?>> packetHandlers) {

        return new UOChannelInitializer(
                clientConnectedHandlerAdapter,
                packetHandlers
        );
    }

    @Bean
    public ServerBootstrap serverBootstrap(
            UOChannelInitializer channelInitializer,
            @Qualifier("parent") NioEventLoopGroup parentGroup,
            @Qualifier("child") NioEventLoopGroup childGroup) {

        return new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    @Bean
    public ServerStartup serverStartup(
            ServerBootstrap serverBootstrap,
            @Qualifier("parent") NioEventLoopGroup parentGroup,
            @Qualifier("child") NioEventLoopGroup childGroup) {

        return new ServerStartup(serverBootstrap, parentGroup, childGroup);
    }
}
