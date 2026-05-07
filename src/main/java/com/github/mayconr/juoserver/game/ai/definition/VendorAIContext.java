package com.github.mayconr.juoserver.game.ai.definition;

import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.World;
import lombok.Getter;

@Getter
public class VendorAIContext extends AIFlowContext {

    private final String stockType;

    public VendorAIContext(UONpc npc, World world, String stockType) {
        super(npc, world);
        this.stockType = stockType;
    }

}
