package com.github.mayconr.juoserver.game.npc.flow.creation;

import com.github.mayconr.juoserver.game.flow.NpcCreationFlowDefinition.NpcCreationContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemModuleImpl;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.FlowPhase;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.HashMap;
import java.util.Map;

public class CreateNpcEquippedItemsStep extends AbstractFlowStep<NpcCreationContext> {

    private final ItemModule itemModule;

    public CreateNpcEquippedItemsStep(int order, ItemModule itemModule) {
        super("CreateNpcEquippedItems", order, FlowPhase.CORE);
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        var template = context.getTemplate();

        final Map<Layer, Integer> equippedItems = new HashMap<>();
        for (String itemName : template.equippedItems()) {
            final var request = ItemRequest.byName(itemName);
            final var target = ItemTarget.orphan();

            // TODO must create item without reference, which means: no container, no equip and not ground. should only be cached
            final var item = itemModule.createItem(request, target);

            equippedItems.put(item.getLayer(), item.getSerialId());
        }
        context.setEquippedItems(equippedItems);
        return StepResult.success();
    }
}
