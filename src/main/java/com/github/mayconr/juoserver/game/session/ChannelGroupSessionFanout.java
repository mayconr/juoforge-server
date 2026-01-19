package com.github.mayconr.juoserver.game.session;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelGroupSessionFanout implements SessionFanout {

    private final ChannelGroup channelGroup;

    @Override
    public void writeAndFlush(Object message) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            channel.eventLoop().execute(() -> {
                channel.writeAndFlush(message);
            });
        }
    }

    @Override
    public void write(Object message) {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            channel.eventLoop().execute(() -> {
                channel.write(message);
            });
        }
    }

    @Override
    public void flush() {
        for (var channel : channelGroup) {
            if (!channel.isActive()) {
                continue;
            }
            channel.eventLoop().execute(channel::flush);
        }
    }
}
