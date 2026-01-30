package com.github.mayconr.shard.actions;

import com.github.mayconr.juoserver.common.event.EventHandler;
import com.github.mayconr.juoserver.common.event.HelpButtonPressed;

public class HelpRequested implements EventHandler<HelpButtonPressed> {
    @Override
    public void handle(HelpButtonPressed event) {
        System.out.println(event+" help");
    }
}
