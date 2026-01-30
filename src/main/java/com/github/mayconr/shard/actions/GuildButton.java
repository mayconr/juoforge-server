package com.github.mayconr.shard.actions;

import com.github.mayconr.juoserver.common.event.EventHandler;
import com.github.mayconr.juoserver.common.event.GuildButtonPressed;

public class GuildButton implements EventHandler<GuildButtonPressed> {
    @Override
    public void handle(GuildButtonPressed event) {
        System.out.println("guild "+event);
    }
}
