package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class SkillHandler {

    private final EventBus eventBus;

    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        var skillList = new ArrayList<SkillValue>();
        for (SkillValue skill : skills) {
            final var newValue = player.getSkills().updateSkill(skill.getSkillId(), skill.getLock());
            skillList.add(newValue);
        }
        eventBus.publish(new SkillLocked(player, skillList));
    }
}
