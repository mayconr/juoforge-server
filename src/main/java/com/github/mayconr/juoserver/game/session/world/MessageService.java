package com.github.mayconr.juoserver.game.session.world;

import com.github.mayconr.juoserver.game.model.TextType;
import com.github.mayconr.juoserver.network.packet.SendSpeech;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageService {

    private final ChannelGroup channelGroup;

    public void handleSendBreadcastMessage(String message) {
        channelGroup.writeAndFlush(
                new SendSpeech(TextType.BROADCAST, 2046, 0, 0, 1, "System", message));
    }
}
