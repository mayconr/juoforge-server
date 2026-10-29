package com.github.mayconr.juoserver.game.skill.flow.gain;

import com.github.mayconr.juoserver.game.skill.SkillGainCalculator;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;
import com.github.mayconr.juoserver.infrastructure.rng.RNG;

public final class TryGainSkillFlowDefinition {
    private TryGainSkillFlowDefinition() {}

    public static Flow<TryGainSkillContext> build(SkillGainCalculator calculator, RNG rng, EventBus events) {
        return FlowFactory.<TryGainSkillContext>builder()
                .step(new ResolveGainSkillStep())
                .step(new RollSkillGainStep(calculator, rng))
                .step(new CalculateSkillGainStep(calculator))
                .step(new ApplySkillGainStep())
                .step(new NotifySkillGainStep(events), TryGainSkillContext::isClientBaseIncreased)
                .build();
    }
}
