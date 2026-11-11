package com.github.mayconr.juoserver.game.messaging;

import com.github.mayconr.juoserver.game.model.UOObject;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;

public interface WorldMessage {
    void send(UOPlayer player, MessageContent message);

    /** Sends a message using a named style registered by the shard. */
    void send(UOPlayer player, MessageContent message, String styleName);

    void send(UOPlayer player, String message);

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
