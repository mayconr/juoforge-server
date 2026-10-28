package com.github.mayconr.juoserver.game.skill;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.skill.flow.use.UseSkillContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import lombok.RequiredArgsConstructor;
import com.github.mayconr.juoserver.game.skill.flow.lock.SendSkillsLockContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;

import java.util.Collection;

@RequiredArgsConstructor
public class SkillModuleImpl implements SkillModule {

    private final SkillSystem skillSystem;

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void sendSkillsLock(UOPlayer player, Collection<SkillValue> skills) {
        var result = flows.execute(new SendSkillsLockContext(player, skills));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("sendSkillsLock", result, null);
        }
    }

    @Override
    public void useSkill(UOPlayer player, int skillId) {
        flows.execute(new UseSkillContext(player, skillId));
    }

    @Override
    public void tryGain(UOMobile mobile, int skillId, double difficulty, SkillGainContext context) {
        skillSystem.tryGain(mobile, skillId, difficulty, context);
    }
}
