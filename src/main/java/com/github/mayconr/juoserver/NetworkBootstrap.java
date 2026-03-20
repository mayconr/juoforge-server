package com.github.mayconr.juoserver;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.world.WorldInternal;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.server.ClientConnectedHandlerAdapter;
import com.github.mayconr.juoserver.infrastructure.server.ServerStartup;
import com.github.mayconr.juoserver.infrastructure.server.UOChannelInitializer;
import com.github.mayconr.juoserver.network.session.NettySessionManager;
import com.github.mayconr.juoserver.network.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class NetworkBootstrap {
    private final EventBus eventBus;
    private final WorldInternal worldInternal;
    private final GamePlaySettings settings;

    public NetworkBootstrap(ServerRuntime runtime) {
        this.eventBus = runtime.eventBus();
        this.worldInternal = (WorldInternal) runtime.world();
        this.settings = runtime.settings();
    }

    public ServerStartup build() {
        ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        SessionManager sessionManager = new NettySessionManager(worldInternal, channelGroup, settings, eventBus);

        NioEventLoopGroup parentGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup childGroup = new NioEventLoopGroup(1);

        ClientConnectedHandlerAdapter connectedHandler = new ClientConnectedHandlerAdapter(channelGroup, sessionManager);

        var packetHandlers = new DefaultPacketHandlerFactory().create(
                worldInternal
        );

        UOChannelInitializer channelInitializer = new UOChannelInitializer(
                connectedHandler,
                packetHandlers
        );

        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return new ServerStartup(serverBootstrap, parentGroup, childGroup);
    }
}
