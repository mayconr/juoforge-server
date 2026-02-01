package com.github.mayconr.juoserver.game.session.player.skill;

import com.github.mayconr.juoserver.common.event.EventBus;
import com.github.mayconr.juoserver.common.event.SkillLocked;
import com.github.mayconr.juoserver.common.event.UseSkillRequested;
import com.github.mayconr.juoserver.game.model.SendSkillType;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.session.SessionOutbound;
import com.github.mayconr.juoserver.network.packet.SendSkill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PlayerSkillService {

    private final UOPlayer player;
    private final SessionOutbound outbound;
    private final EventBus eventBus;

    public void useSkill(int skillId) {
        if (skillId < 0) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Skill [skillId={}] use requested by [{}-{}]", skillId, player.getSerialId(), player.getName());
        }

        eventBus.publish(new UseSkillRequested(player, skillId));
    }

    public void sendGumpDialog(int serialId) {
        int dataSerial = serialId;
        String dataName;

        if (serialId == player.getSerialId()) {
            dataName = player.getName();
            outbound.writeAndFlush(new SendSkill(SendSkillType.FULL_LIST, player.getSkills().skills()));
        } else {
            dataName = "Unknown";
            log.info("Not implemented yet");
        }

        if (log.isDebugEnabled()) {
            log.debug("Sending skill gump with [{}-{}] data for [{}-{}]", serialId, dataName, player.getSerialId(), player.getName());
        }
    }

    public void updateSkillsLock(Collection<SkillValue> skills) {
        for (SkillValue skill : skills) {
            final var newValue = player.getSkills().updateSkill(skill.getSkillId(), skill.getLock());
            outbound.write(new SendSkill(SendSkillType.SINGLE_UPDATE, List.of(newValue)));
            eventBus.publish(new SkillLocked(player, newValue));
        }
        outbound.flush();
    }

    public void sendSkill(SkillValue value) {
        outbound.writeAndFlush(new SendSkill(SendSkillType.SINGLE_UPDATE, List.of(value)));
    }
}
