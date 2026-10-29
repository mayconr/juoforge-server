package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class ValidateRegenStep extends AbstractFlowStep<RegenContext> {
    private final GamePlaySettings settings;

    public ValidateRegenStep(GamePlaySettings settings) {
        super("validate-regen");
        this.settings = settings;
    }

    @Override
    public StepResult execute(RegenContext context) {
        var mobile = context.getMobile();
        if (mobile == null) {
            return StepResult.failure("MISSING_MOBILE", "Regeneration target is required");
        }
        if (!Double.isFinite(context.getElapsedSeconds()) || context.getElapsedSeconds() < 0) {
            return StepResult.failure("INVALID_INTERVAL", "Elapsed seconds must be finite and non-negative");
        }
        if (settings.mobile().vitals().saturationFactor() <= 0) {
            return StepResult.failure("INVALID_REGEN_SETTINGS", "Vitals saturation factor must be positive");
        }
        if (!mobile.isAlive() || mobile instanceof UOPlayer player && !player.isConnected()) {
            mobile.runtimeAttributes().remove(RegenContext.HP_ACCUMULATOR);
            mobile.runtimeAttributes().remove(RegenContext.STAMINA_ACCUMULATOR);
            mobile.runtimeAttributes().remove(RegenContext.MANA_ACCUMULATOR);
            return StepResult.stop("INELIGIBLE_MOBILE", "Dead or offline mobiles do not regenerate");
        }
        if (context.getElapsedSeconds() == 0) {
            return StepResult.stop("ZERO_INTERVAL", "No time elapsed");
        }
        return StepResult.success();
    }
}
