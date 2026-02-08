package com.github.mayconr.shard.skills;

import com.github.mayconr.juoserver.game.event.EventHandler;
import com.github.mayconr.juoserver.game.model.event.UseSkillRequested;

public class Anatomy implements EventHandler<UseSkillRequested> {
    @Override
    public void handle(UseSkillRequested event) {
        System.out.println("using anatomy");
    }
}
