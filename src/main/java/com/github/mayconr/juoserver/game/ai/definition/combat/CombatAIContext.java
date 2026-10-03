package com.github.mayconr.juoserver.game.ai.definition.combat;

import com.github.mayconr.juoserver.game.ai.definition.AIFlowContext;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.World;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Persistent combat decision state; populated by the future combat AI steps. */
@Getter
@Setter
public class CombatAIContext extends AIFlowContext {
    private CombatAIState state = CombatAIState.IDLE;
    private UOPlayer target;
    private List<UOPlayer> nearbyPlayers = List.of();

    public CombatAIContext(UONpc npc, World world) {
        super(npc, world);
    }
}
