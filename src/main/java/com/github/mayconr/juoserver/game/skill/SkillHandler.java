package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.SkillLocked;
import com.github.mayconr.juoserver.game.model.event.UseSkillRequested;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class SkillHandler {

    private final EventBus eventBus;
    private final RealmStorage storage;

    public void useSkill(UOPlayer player, int skillId) {
        if (skillId < 0) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Skill [skillId={}] use requested by [{}-{}]", skillId, player.getSerialId(), player.getName());
        }

        eventBus.publish(new UseSkillRequested(player, skillId));
    }

    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        var skillList = new ArrayList<SkillValue>();
        for (SkillValue skill : skills) {
            final var newValue = player.getSkills().updateSkill(skill.getSkillId(), skill.getLock());
            skillList.add(newValue);
        }
        eventBus.publish(new SkillLocked(player, skillList));
    }
}
