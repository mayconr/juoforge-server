package com.github.mayconr.juoserver.game.mobile.flow.gold;

import com.github.mayconr.juoserver.game.wallet.Wallet;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class CalculateGoldStep extends AbstractFlowStep<RecalculateGoldContext> {
    private final Wallet wallet;

    public CalculateGoldStep(Wallet wallet) {
        super("calculate-mobile-gold");
        this.wallet = wallet;
    }

    @Override
    public StepResult execute(RecalculateGoldContext context) {
        var mobile = context.getMobile();
        if (mobile == null) return StepResult.failure("MISSING_MOBILE", "Mobile is required to recalculate gold");
        context.setOldBalance(mobile.getGold());
        mobile.setGold(wallet.getBalance(mobile));
        return StepResult.success();
    }
}
