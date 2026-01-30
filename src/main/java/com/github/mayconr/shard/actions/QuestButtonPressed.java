package com.github.mayconr.shard.actions;

import com.github.mayconr.juoserver.common.event.EventHandler;

public class QuestButtonPressed implements EventHandler<com.github.mayconr.juoserver.common.event.QuestButtonPressed> {
    @Override
    public void handle(com.github.mayconr.juoserver.common.event.QuestButtonPressed event) {
        System.out.println("Quest "+event.player());
    }
}
