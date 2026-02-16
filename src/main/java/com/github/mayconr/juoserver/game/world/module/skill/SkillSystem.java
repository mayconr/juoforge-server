package com.github.mayconr.juoserver.game.world.module.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;

public interface SkillSystem {

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);

}
