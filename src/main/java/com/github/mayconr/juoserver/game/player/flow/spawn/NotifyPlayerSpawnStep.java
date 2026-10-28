package com.github.mayconr.juoserver.game.player.flow.spawn;

import com.github.mayconr.juoserver.game.model.event.PlayerLoggedIn;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class NotifyPlayerSpawnStep extends AbstractFlowStep<PlayerSpawnContext> {
    private final EventBus events;

    public NotifyPlayerSpawnStep(EventBus events) {
        super("NotifyPlayerSpawn");
        this.events = events;
    }

    @Override
    public StepResult execute(PlayerSpawnContext context) {
        events.publish(new PlayerLoggedIn(context.getPlayer()));
        return StepResult.success();
    }
}
