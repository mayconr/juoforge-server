package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.game.model.SkillGainContext;
import com.github.mayconr.juoserver.game.model.SkillValue;
import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractSyncFlowContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public final class TryGainSkillContext extends AbstractSyncFlowContext<Void> {
    private final UOMobile mobile;
    private final int skillId;
    private final double difficulty;
    private final SkillGainContext gainContext;
    @Setter
    private SkillValue skill;
    @Setter
    private double gainAmount;
    @Setter
    private boolean clientBaseIncreased;
}
