package com.github.mayconr.shard.actions;

import com.github.mayconr.juoserver.game.event.EventHandler;
import com.github.mayconr.juoserver.game.model.event.HelpButtonPressed;

public class HelpRequested implements EventHandler<HelpButtonPressed> {
    @Override
    public void handle(HelpButtonPressed event) {
        System.out.println(event+" help");
    }
}
