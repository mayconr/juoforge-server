package com.github.mayconr.juoserver.game.world.module.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;

public interface SkillCommands {

    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

    void useSkill(UOPlayer player, int skillId);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);
}
