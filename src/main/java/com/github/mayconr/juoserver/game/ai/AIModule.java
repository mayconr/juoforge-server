package com.github.mayconr.juoserver.game.ai;

import com.github.mayconr.juoserver.game.world.WorldModule;
import com.github.mayconr.juoserver.infrastructure.region.RegionNode;
import com.github.mayconr.juoserver.game.model.UONpc;

public interface AIModule extends WorldAI, WorldModule {
    /** Activates attached AI sessions for living NPCs inside the region. */
    void activateRegion(RegionNode region);

    /** Deactivates attached AI sessions in the region only if no players remain in it. */
    void deactivateRegion(RegionNode region);

    /** Pauses an attached NPC session without detaching it. */
    void deactivate(UONpc npc);
}
