package com.github.mayconr.juoserver.game.messaging;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;

public interface MessagingOperations {
    void send(UOPlayer player, MessageContent message);

    void printTextAbove(UOObject source, MessageContent message);

    /**
     * Print text above the source only for the informed player
     * @param source
     * @param message
     * @param player
     */
    void printTextAbove(UOObject source, MessageContent message, UOPlayer player);

    void broadcast(MessageContent message);

}
