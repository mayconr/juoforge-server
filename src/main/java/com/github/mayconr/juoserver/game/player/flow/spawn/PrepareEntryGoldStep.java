package com.github.mayconr.juoserver.game.player.flow.spawn;

import com.github.mayconr.juoserver.game.mobile.MobileModule;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

final class PrepareEntryGoldStep extends AbstractFlowStep<PlayerSpawnContext> {
    private final MobileModule mobiles;

    PrepareEntryGoldStep(MobileModule mobiles) {
        super("prepare-entry-gold");
        this.mobiles = mobiles;
    }

    @Override
    public StepResult execute(PlayerSpawnContext context) {
        mobiles.recalculateGold(context.getPlayer(), false);
        return StepResult.success();
    }
}
