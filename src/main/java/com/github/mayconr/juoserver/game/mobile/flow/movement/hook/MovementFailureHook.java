package com.github.mayconr.juoserver.game.mobile.flow.movement.hook;

import com.github.mayconr.juoserver.game.mobile.flow.movement.MovementContext;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.model.event.MobileMoveRejected;
import com.github.mayconr.juoserver.infrastructure.eventbus.EventBus;
import com.github.mayconr.juoserver.infrastructure.flow.*;
import lombok.extern.slf4j.Slf4j;

public class MovementFailureHook extends FlowHook<MovementContext> {

    public MovementFailureHook(EventBus eventBus) {
        super(HookTarget.any(), new TesteHookStep(eventBus), HookPosition.AFTER_FAILURE);
    }

    @Slf4j
    private record TesteHookStep(EventBus eventBus) implements HookStep<MovementContext> {

        @Override
        public void execute(String stepName, StepResult result, MovementContext context) {
            final var mobile = context.getMobile();

            if (mobile instanceof UOPlayer player) {
                if (log.isDebugEnabled()) {
                    log.debug("Player {} cannot move due to: {}", mobile.getName(), result.reason());
                }
                player.movementSequence(0);
                eventBus.publish(new MobileMoveRejected(mobile, context.getSequence()));
            }

        }
    }

}
