package com.github.mayconr.juoserver.game.packet.handler;

import com.github.mayconr.juoserver.game.packet.PingPong;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class PingPongHandler extends SimpleChannelInboundHandler<PingPong> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PingPong msg) throws Exception {
        ctx.writeAndFlush(new PingPong(msg.getSequence()));
    }
}
