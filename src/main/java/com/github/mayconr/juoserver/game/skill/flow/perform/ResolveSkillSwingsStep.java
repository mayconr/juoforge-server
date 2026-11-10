package com.github.mayconr.juoserver.game.skill.flow.perform;

import com.github.mayconr.juoserver.game.mobile.WorldMobile;
import com.github.mayconr.juoserver.game.skill.WorldSkill;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ResolveSkillSwingsStep extends AbstractFlowStep<PerformSkillContext> {
    private final WorldMobile mobiles;

    public ResolveSkillSwingsStep(WorldMobile mobiles) {
        super("resolve-skill-swings");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(PerformSkillContext context) {
        var override = context.getOptions().swingCount();
        var value = override != null
                ? override
                : mobiles.getProperty(context.getPlayer(), WorldSkill.SWINGS_PROPERTY, false);
        context.setSwings(swingCount(value));
        return StepResult.success();
    }

    private int swingCount(Object value) {
        if (value instanceof Number number) {
            double count = number.doubleValue();
            if (Double.isFinite(count) && count <= Integer.MAX_VALUE && count == Math.rint(count)) {
                return Math.max(1, (int) count);
            }
        }
        return 1;
    }
}
