package com.github.mayconr.shard.actions;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;

public class QuestButtonPressed implements EventHandler<com.github.mayconr.juoserver.game.model.event.QuestButtonPressed> {
    @Override
    public void handle(com.github.mayconr.juoserver.game.model.event.QuestButtonPressed event) {
        System.out.println("Quest "+event.player());
    }
}
