package com.github.mayconr.juoserver.game.ai.definition.aggressive;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
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
    private List<UOPlayer> nearbyPlayers = List.of();

    public AggressiveAIContext(UONpc npc, World world) {
        super(npc, world);
    }
}
