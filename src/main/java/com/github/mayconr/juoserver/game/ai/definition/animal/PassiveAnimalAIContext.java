package com.github.mayconr.juoserver.game.ai.definition.animal;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;

public class PassiveAnimalAIContext extends AIFlowContext {

    private final FleeContext flee = new FleeContext();

    public PassiveAnimalAIContext(UONpc npc, World world) {
        super(npc, world);
    }

    public FleeContext flee() {
        return flee;
    }

    public final class FleeContext {

        private static final int FLEE_RANGE = 4;

        private UOPlayer nearbyPlayer;
        private boolean alerted;

        public UOPlayer nearbyPlayer() {
            return nearbyPlayer;
        }

        public void updateNearbyPlayer(UOPlayer player) {
            nearbyPlayer = player;
            if (player == null) {
                alerted = false;
            }
        }

        public int distanceTo(UOPlayer player) {
            int dx = Math.abs(npc().getX() - player.getX());
            int dy = Math.abs(npc().getY() - player.getY());
            return Math.max(dx, dy);
        }

        public boolean shouldFlee() {
            return nearbyPlayer != null && distanceTo(nearbyPlayer) <= FLEE_RANGE;
        }

        public boolean shouldAlert() {
            return nearbyPlayer != null && !shouldFlee() && !alerted;
        }

        public void markAlerted() {
            alerted = true;
        }
    }
}
