package com.github.mayconr.juoserver.game.core.session;

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

}
