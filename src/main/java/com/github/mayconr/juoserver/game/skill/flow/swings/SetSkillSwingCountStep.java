package com.github.mayconr.juoserver.game.skill.flow.swings;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.skill.WorldSkill;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class SetSkillSwingCountStep extends AbstractFlowStep<SetSkillSwingCountContext> {
    private final WorldMobile mobiles;

    public SetSkillSwingCountStep(WorldMobile mobiles) {
        super("set-skill-swing-count");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(SetSkillSwingCountContext context) {
        mobiles.setProperty(
                context.getPlayer(), WorldSkill.SWINGS_PROPERTY, Math.max(1, context.getSwingCount()), false
        );
        return StepResult.success();
    }
}
