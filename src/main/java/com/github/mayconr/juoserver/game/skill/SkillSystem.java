package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.world.WorldInternal;

public interface SkillSystem {

    void initialize(WorldInternal worldInternal);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);

}
