package com.github.mayconr.juoserver.game.world.module.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.WorldModule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class SkillModule implements WorldModule, SkillCommands {

    private final SkillHandler skillHandler;
    private final SkillSystem skillSystem;

    @Override
    public void update(double delta) {

    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        skillHandler.sendSkillsLock(player, skills);
    }

    @Override
    public void useSkill(UOPlayer player, int skillId) {
        skillHandler.useSkill(player, skillId);
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillSystem.tryGain(mobile, skillId, difficulty, context);
    }
}
