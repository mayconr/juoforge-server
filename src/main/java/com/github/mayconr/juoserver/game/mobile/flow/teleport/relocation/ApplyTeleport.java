package com.github.mayconr.juoserver.game.mobile.flow.teleport.relocation;

import com.github.mayconr.juoserver.game.mobile.flow.teleport.TeleportContext;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

public class ApplyTeleport extends AbstractFlowStep<TeleportContext> {

    private final RealmStorage storage;

    public ApplyTeleport(RealmStorage storage) {
        super("ApplyTeleport");
        this.storage = storage;
    }

    @Override
    public StepResult execute(TeleportContext context) {
        final var mobile = context.getMobile();
        final var from = new PointInTheWorld(mobile);
        context.setFrom(from);
        final var to = context.getLocation();
        final var direction = context.getDirection();

        synchronized (this) {
            mobile.setDirection(direction);
            mobile.setRunning(false);
            mobile.setLocation(to);
            if (mobile instanceof UOPlayer player) {
                // ClassicUO resets its walk sequence when receiving DrawGamePlayer (0x20).
                player.movementSequence(0);
            }
            storage.updateMobileLocation(mobile, from, to);
        }

        return StepResult.success();
    }
}
