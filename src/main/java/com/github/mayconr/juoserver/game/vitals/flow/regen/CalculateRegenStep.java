package com.github.mayconr.juoserver.game.vitals.flow.regen;

import com.github.mayconr.juoserver.game.GamePlaySettings;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenContext.ResourceRegen;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class CalculateRegenStep extends AbstractFlowStep<RegenContext> {
    private final GamePlaySettings settings;

    public CalculateRegenStep(GamePlaySettings settings) {
        super("calculate-regen");
        this.settings = settings;
    }

    @Override
    public StepResult execute(RegenContext context) {
        var mobile = context.getMobile();
        context.setHitpoints(calculate(context, RegenContext.HP_ACCUMULATOR,
                mobile.getHitpoints(), mobile.getMaxHitpoints(), mobile.getStrength()));
        context.setStamina(calculate(context, RegenContext.STAMINA_ACCUMULATOR,
                mobile.getStamina(), mobile.getMaxStamina(), mobile.getDexterity()));
        context.setMana(calculate(context, RegenContext.MANA_ACCUMULATOR,
                mobile.getMana(), mobile.getMaxMana(), mobile.getIntelligence()));
        return StepResult.success();
    }

    private ResourceRegen calculate(RegenContext context, String key, int current, int maximum, int stat) {
        if (current >= maximum) {
            return new ResourceRegen(current, current, 0.0);
        }
        double effectiveStat = Math.max(0, stat);
        double rate = 0.1 + effectiveStat / (effectiveStat + settings.vitals().saturationFactor());
        double accumulated = context.getMobile().runtimeAttributes().getOrDefault(key, 0.0)
                + rate * context.getElapsedSeconds();
        int gain = (int) Math.min(Math.floor(accumulated + 1e-9), (double) maximum - current);
        int next = current + gain;
        double remainder = next == maximum ? 0.0 : Math.max(0.0, accumulated - gain);
        return new ResourceRegen(current, next, remainder);
    }
}
