package com.github.mayconr.juoserver.game.world;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;

import java.util.Collection;

public interface SkillInternal {

    void useSkill(UOPlayer player, int skillId);

    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);
}
