package com.github.mayconr.juoserver.game.mobile.flow.resync.resolver;

import com.github.mayconr.juoserver.game.mobile.flow.resync.ResyncContext;
import com.github.mayconr.juoserver.game.model.event.MobileMoveResync;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public class ResolveResyncSequenceStep extends AbstractFlowStep<ResyncContext> {

    private final EventBus eventBus;

    public ResolveResyncSequenceStep(EventBus eventBus) {
        super("ResolveResyncSequence");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(ResyncContext context) {
        var player = context.getPlayer();
        var clientSequence = context.getResyncAck().getSequence();

        player.movementSequence(clientSequence);
        eventBus.publish(new MobileMoveResync(player, clientSequence));
        //System.out.println("Server sequence "+sequence+" client sequence "+context.getResyncAck().getSequence());

        return StepResult.success();
    }
}
