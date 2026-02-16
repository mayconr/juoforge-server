package com.github.mayconr.juoserver.game.world.module.ui;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.MessageOptions;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MessageSent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageHandler {

    private final EventBus eventBus;

    public void sendMessage(UOPlayer player, String text, MessageOptions options) {
        eventBus.publish(new MessageSent(player, text, options));
    }

    public void handleSendBreadcastMessage(String message) {
        //channelGroup.writeAndFlush(
        //        new SendSpeech(TextType.BROADCAST, 2046, 0, 0, 1, "System", message));
    }
}
