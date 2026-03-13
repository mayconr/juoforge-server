package com.github.mayconr.shard.skills;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventHandler;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;

public class SkillLockedHandler implements EventHandler<SkillLocked> {
    @Override
    public void handle(SkillLocked event) {
        System.out.println("Skill "+event.skills() +" was locked");
    }
}
