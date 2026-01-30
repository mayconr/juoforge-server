package com.github.mayconr.juoserver.game.session.player.message;

import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.SendSpeech;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerMessageService {

    private final SessionOutbound outbound;

    public void sendMessage(String message, MessageOptions options) {

        var speakerId = 0;
        var speakerName = "";
        var speakerModel = 0;
        if (options.object() != null) {
            speakerId = options.object().getSerialId();
            speakerName = options.object().getDisplayName();
            speakerModel = options.object().getModelId();
        }
        outbound.writeAndFlush(new SendSpeech(options.type(), options.hue(), speakerId, speakerModel, options.font(), speakerName, message));
    }

}
