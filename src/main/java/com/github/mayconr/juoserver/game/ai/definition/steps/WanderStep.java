package com.github.mayconr.juoserver.game.ai.definition.steps;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.concurrent.ThreadLocalRandom;

public class WanderStep<T extends AIFlowContext> extends AbstractFlowStep<T> {

    private final double moveInterval;

    public WanderStep(double moveInterval) {
        super("WanderStep");
        this.moveInterval = moveInterval;
    }

    @Override
    public StepResult execute(AIFlowContext ctx) {
        double acc = ctx.getOrDefault("wander.acc", 0.0);
        acc += ctx.delta();

        if (acc < moveInterval) {
            ctx.set("wander.acc", acc);
            return StepResult.success();
        }

        ctx.set("wander.acc", 0.0);

        var directions = Direction.values();
        var dir = directions[ThreadLocalRandom.current().nextInt(directions.length)];

        ctx.enqueueAction(new WalkAction(ctx.npc(), dir));

        return StepResult.success();
    }
}
