package com.github.mayconr.juoserver.game.interaction.flow.target.send.state;

import com.github.mayconr.juoserver.game.interaction.flow.target.send.SendTargetContext;
import com.github.mayconr.juoserver.game.model.CursorTarget;
import com.github.mayconr.juoserver.game.model.event.TargetSent;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SendTargetStep extends AbstractFlowStep<SendTargetContext> {

    private final AtomicInteger sequenceGenerator = new AtomicInteger(1);
    private final EventBus eventBus;

    public SendTargetStep(EventBus eventBus) {
        super("SendTarget");
        this.eventBus = eventBus;
    }

    @Override
    public StepResult execute(SendTargetContext context) {
        final var player = context.getPlayer();
        final var type = context.getType();
        final var attributes = context.getPlayer().runtimeAttributes();
        final var sequence = sequenceGenerator.getAndIncrement();
        final var consumer = context.getConsumer();

        attributes.set("TARGET_"+ sequence, consumer);

        if (log.isDebugEnabled()) {
            log.debug("Target [{}] sent to client", sequence);
        }

        eventBus.publish(new TargetSent(player, sequence, CursorTarget.LOCATION, type));

        return StepResult.success();
    }
}
