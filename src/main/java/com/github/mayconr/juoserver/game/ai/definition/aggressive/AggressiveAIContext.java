package com.github.mayconr.juoserver.game.ai.definition.aggressive;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.Direction;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.PointInTheWorld;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Persistent decision state for aggressive NPC behavior. */
@Getter
@Setter
public class AggressiveAIContext extends AIFlowContext {
    private CombatAIState state = CombatAIState.IDLE;
    private UOPlayer target;
    private final PursuitContext pursuit = new PursuitContext();
    private List<UOPlayer> nearbyPlayers = List.of();

    public AggressiveAIContext(UONpc npc, World world) {
        super(npc, world);
    }

    @Getter
    @Setter
    public static final class PursuitContext {
        private Direction nextDirection;
        private boolean movementBlocked;
        private PursuitPath path;

        public boolean hasPathFor(Location origin, Location destination) {
            return path != null
                    && path.origin().equals(new PointInTheWorld(origin))
                    && path.destination().equals(new PointInTheWorld(destination));
        }
    }

    public record PursuitPath(PointInTheWorld origin, PointInTheWorld destination, List<Direction> directions) {
    }
}
