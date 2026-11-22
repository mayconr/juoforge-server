package com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class CheckPursuitMovementStep extends AbstractFlowStep<AggressiveAIContext> {
    private final UOFileReader fileReader;

    public CheckPursuitMovementStep(UOFileReader fileReader) {
        super("CheckPursuitMovementStep");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        var pursuit = context.getPursuit();
        var direction = pursuit.getNextDirection();
        if (direction == null) {
            return StepResult.skip();
        }

        var npc = context.npc();
        var walkZ = fileReader.resolveWalkZ(npc, direction);
        int x = npc.getX() + direction.getDx();
        int y = npc.getY() + direction.getDy();
        pursuit.setMovementBlocked(walkZ.isEmpty() || fileReader.hasBlockingCollision(npc, x, y, walkZ.getAsInt()));
        return StepResult.success();
    }
}
