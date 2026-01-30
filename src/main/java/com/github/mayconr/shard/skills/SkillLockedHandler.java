package com.github.mayconr.shard.skills;

import com.github.mayconr.juoserver.common.event.EventHandler;
import com.github.mayconr.juoserver.common.event.SkillLocked;

public class SkillLockedHandler implements EventHandler<SkillLocked> {
    @Override
    public void handle(SkillLocked event) {
        System.out.println("Skill "+event.value() +" was locked");
    }
}
