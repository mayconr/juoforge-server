package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.ai.definition.shared.steps.WanderStep;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WanderStepTest {

    @Test
    void changesDirectionWithinSixWalkActions() {
        var context = new AIFlowContext(mock(UONpc.class), mock(World.class));
        var step = new WanderStep<AIFlowContext>(2.0);
        Direction previous = null;
        int consecutiveSteps = 0;

        context.setDelta(1.0);
        for (int action = 0; action < 48; action++) {
            step.execute(context);
            assertTrue(context.actions().isEmpty());
            step.execute(context);
            var walk = assertInstanceOf(WalkAction.class, context.actions().poll());

            consecutiveSteps = walk.direction() == previous ? consecutiveSteps + 1 : 1;
            assertTrue(consecutiveSteps <= 6);
            previous = walk.direction();
        }
    }
}
