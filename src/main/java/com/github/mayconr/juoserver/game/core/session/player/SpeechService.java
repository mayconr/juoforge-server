package com.github.mayconr.juoserver.game.core.session.player;

import com.github.mayconr.juoserver.game.core.event.EventBus;
import com.github.mayconr.juoserver.game.core.event.MobileSpeech;
import com.github.mayconr.juoserver.game.core.event.Prompt;
import com.github.mayconr.juoserver.game.core.model.UOMobile;
import com.github.mayconr.juoserver.game.packet.SendSpeech;
import com.github.mayconr.juoserver.game.packet.UnicodeSpeachRequest;

import io.netty.channel.group.ChannelGroup;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class SpeechService {

    private final UOMobile mobile;
    private final EventBus eventBus;
    private final ChannelGroup channelGroup;

    public void handleSpeech(UnicodeSpeachRequest request) {
        final var text = request.getText();
        if (text.startsWith(".")) {
            eventBus.publish(Prompt.newInstance(mobile, text));
        } else {
            channelGroup.writeAndFlush(new SendSpeech(mobile, request));
            eventBus.publish(new MobileSpeech(mobile, text));
        }
    }
}
