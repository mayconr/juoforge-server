package com.github.mayconr.juoserver.game.vitals;

import com.github.mayconr.juoserver.game.model.UOMobile;
import com.github.mayconr.juoserver.game.player.WorldPlayer;
import com.github.mayconr.juoserver.game.vitals.flow.regen.RegenContext;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;
import com.github.mayconr.juoserver.game.world.ModuleOperationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VitalsModuleImpl implements VitalsModule {
    private final WorldPlayer players;
    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public void update(double delta) {
        validateInterval(delta);
        for (var player : players.getOnlinePlayers()) {
            regen(player, delta);
        }
    }

    @Override
    public void regen(UOMobile mobile, double elapsedSeconds) {
        validateInterval(elapsedSeconds);
        var result = flows.execute(new RegenContext(mobile, elapsedSeconds));
        if (result.flowFailed()) {
            throw ModuleOperationException.fromFailure("regen", result, null);
        }
    }

    private void validateInterval(double elapsedSeconds) {
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds < 0) {
            throw new IllegalArgumentException("Elapsed seconds must be finite and non-negative");
        }
    }
}
