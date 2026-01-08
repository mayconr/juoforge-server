package com.github.mayconr.juoserver.game.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;

public class ErrorHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            LOGGER.error("I/O error: {}", cause.getMessage());
        } else if (cause instanceof DecoderException) {
            LOGGER.error("Protocol error: {}", cause.getMessage(), cause);
        } else {
            LOGGER.error("Unexpected error: {}", cause.getMessage(), cause);
        }
    }
}
