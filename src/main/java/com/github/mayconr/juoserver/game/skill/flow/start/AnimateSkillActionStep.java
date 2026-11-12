package com.github.mayconr.juoserver.game.skill.flow.start;

import com.github.mayconr.juoserver.game.interaction.WorldInteraction;
import com.github.mayconr.juoserver.game.skill.SkillActionAnimation;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

/** Resolves the per-swing animation and sends it for the first swing cycle. */
public final class AnimateSkillActionStep extends AbstractFlowStep<StartSkillActionContext> {
    private static final int FRAMES_PER_SECOND = 20;
    private final WorldInteraction interaction;

    public AnimateSkillActionStep(WorldInteraction interaction) {
        super("animate-skill-action");
        this.interaction = interaction;
    }

    @Override
    public StepResult execute(StartSkillActionContext context) {
        var group = context.getOptions().animationGroup();
        if (group == null) {
            return StepResult.success();
        }
        var animation = new SkillActionAnimation(group, frames(context.getDurationSeconds()));
        context.setAnimation(animation);
        interaction.sendAnimation(context.getPlayer(), animation.optionsFor(context.getPlayer()));
        return StepResult.success();
    }

    private int frames(double durationSeconds) {
        return Math.max(1, (int) Math.round(durationSeconds * FRAMES_PER_SECOND));
    }
}
