package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;

public interface WorldSkill {
    void useSkill(UOPlayer player, int skillId);

    void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context);
}
