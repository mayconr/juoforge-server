package com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.AggressiveAIContext.PursuitPath;
import com.github.mayconr.juoserver.game.ai.pathfinding.Pathfinder;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.OptionalInt;

@Slf4j
public final class CalculatePursuitPathStep extends AbstractFlowStep<AggressiveAIContext> {
    private static final int MAX_PATH_VISITS = 1024;

    private final UOFileReader fileReader;

    public CalculatePursuitPathStep(UOFileReader fileReader) {
        super("CalculatePursuitPathStep");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(AggressiveAIContext context) {
        var pursuit = context.getPursuit();
        var npc = context.npc();
        var target = context.getTarget();
        var origin = new PointInTheWorld(npc);
        var destination = new PointInTheWorld(target);
        var pathfinder = new Pathfinder((from, direction) -> resolveStep(npc, from, direction), MAX_PATH_VISITS);
        var route = pathfinder.findPath(origin, destination);
        pursuit.setPath(new PursuitPath(origin, destination, route));
        log.info("Pathfinding for NPC [{}] from {} to {} with {} blocked. Result: {}",
                npc.getName(), origin, destination, pursuit.getNextDirection(),
                route.isEmpty() ? "no route found within the search limit" : route);
        return StepResult.success();
    }

    private OptionalInt resolveStep(UONpc npc, Location from, Direction direction) {
        var z = fileReader.resolveWalkZ(from, direction);
        int x = from.getX() + direction.getDx();
        int y = from.getY() + direction.getDy();
        if (z.isEmpty() || fileReader.hasBlockingCollision(npc, x, y, z.getAsInt())) {
            return OptionalInt.empty();
        }
        return z;
    }
}
