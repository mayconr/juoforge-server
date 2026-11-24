package com.github.mayconr.juoserver.game.ai.definition.shared.steps;

import com.github.mayconr.juoserver.game.ai.actions.WalkAction;
import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.concurrent.ThreadLocalRandom;

public class WanderStep<T extends AIFlowContext> extends AbstractFlowStep<T> {

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MAX_STEPS_PER_DIRECTION = 6;
    private static final String ROUTE_KEY = "wander.route";

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

        var route = ctx.get(ROUTE_KEY, WanderRoute.class);
        if (route == null || route.stepsRemaining() == 0) {
            route = chooseRoute(route);
        }

        ctx.enqueueAction(new WalkAction(ctx.npc(), route.direction()));
        ctx.set(ROUTE_KEY, new WanderRoute(route.direction(), route.stepsRemaining() - 1));
        return StepResult.success();
    }

    private WanderRoute chooseRoute(WanderRoute previous) {
        var random = ThreadLocalRandom.current();
        int directionIndex = previous == null
                ? random.nextInt(DIRECTIONS.length)
                : (previous.direction().ordinal() + random.nextInt(1, DIRECTIONS.length)) % DIRECTIONS.length;
        int steps = random.nextInt(1, MAX_STEPS_PER_DIRECTION + 1);
        return new WanderRoute(DIRECTIONS[directionIndex], steps);
    }

    private record WanderRoute(Direction direction, int stepsRemaining) {
    }
}
