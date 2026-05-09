package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;

import java.util.Collection;

public interface SkillModule extends WorldModule {

    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

    void useSkill(UOPlayer player, int skillId);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);
}
