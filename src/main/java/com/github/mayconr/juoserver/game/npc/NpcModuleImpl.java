package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.flow.NpcRemovalFlowDefinition;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;

public class NpcModuleImpl implements NpcModule {

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public UONpc createNpc(String template, Location location) {
        var context = new NpcCreationContext(template, location);
        flows.execute(context);
        return context.getNpc();
    }

    @Override
    public void removeNpc(UONpc uonpc) {
        flows.execute(new NpcRemovalFlowDefinition.NpcRemovalContext(uonpc));
    }
}
