package com.github.mayconr.juoserver.game.npc;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.npc.flow.removal.NpcRemovalContext;
import com.github.mayconr.juoserver.game.model.Location;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.game.model.UOPlayer;
import com.github.mayconr.juoserver.game.world.context.ModuleContext;

public class NpcModuleImpl implements NpcModule {

    private ModuleContext.FlowFacade flows;

    @Override
    public void initialize(ModuleContext context) {
        this.flows = context.flows();
    }

    @Override
    public UONpc createNpc(NpcRequester requester, String template, Location location) {
        var context = new NpcCreationContext(requester, template, location);
        flows.execute(context);
        return context.getNpc();
    }

    @Override
    public void removeNpc(NpcRequester requester, UONpc npc) {
        flows.execute(new NpcRemovalContext(requester, npc));
    }
}
