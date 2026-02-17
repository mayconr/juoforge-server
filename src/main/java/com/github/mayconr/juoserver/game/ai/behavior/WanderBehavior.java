package com.github.mayconr.juoserver.game.ai.behavior;

import com.github.mayconr.juoserver.game.ai.AIContext;
import com.github.mayconr.juoserver.game.ai.action.WalkAction;
import com.github.mayconr.juoserver.game.model.Direction;

import java.util.concurrent.ThreadLocalRandom;

public class WanderBehavior implements Behavior {
    private AIContext context;

    private static final double MOVE_INTERVAL = 2.0; // seconds
    private double accumulator = 0.0;

    @Override
    public void initialize(AIContext context) {
        this.context = context;
    }

    @Override
    public void onThink(double delta) {
        accumulator += delta;

        if (accumulator < MOVE_INTERVAL) {
            return;
        }

        accumulator = 0.0;

        attemptRandomMove();
    }

    private void attemptRandomMove() {
        var mobile = context.npc();

        Direction[] directions = Direction.values();
        Direction direction = directions[ThreadLocalRandom.current().nextInt(directions.length)];

        context.enqueue(new WalkAction(mobile, direction));
    }

}
