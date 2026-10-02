package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;

import java.util.Collection;

public interface SkillModule extends WorldSkill, WorldModule {

    void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills);

}
