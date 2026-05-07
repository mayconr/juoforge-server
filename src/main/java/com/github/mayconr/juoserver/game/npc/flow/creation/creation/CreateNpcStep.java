package com.github.mayconr.juoserver.game.npc.flow.creation.creation;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.game.model.UONpc;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import com.github.mayconr.juoserver.infrastructure.storage.RealmStorage;

import java.util.HashMap;
import java.util.Map;

public class CreateNpcStep extends AbstractFlowStep<NpcCreationContext> {

    private final RealmStorage storage;

    public CreateNpcStep(RealmStorage storage) {
        super("CreateNpc");
        this.storage = storage;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        final var template = context.getTemplate();
        final Map<Layer, Integer> equippedItems = context.getEquippedItems() == null ? new HashMap<>() : context.getEquippedItems();

        var data = template.toData(context.getSerialId(), equippedItems, context.getLocation());
        var npc = (UONpc) storage.createMobile(data);

        context.setNpc(npc);

        return StepResult.success();
    }
}
