package com.github.mayconr.juoserver.game.npc.flow.creation.build;

import com.github.mayconr.juoserver.game.npc.flow.creation.NpcCreationContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.model.ItemLocation;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

import java.util.HashMap;
import java.util.Map;

public class CreateNpcEquippedItemsStep extends AbstractFlowStep<NpcCreationContext> {

    private final ItemModule itemModule;

    public CreateNpcEquippedItemsStep(ItemModule itemModule) {
        super("CreateNpcEquippedItems");
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(NpcCreationContext context) {
        var template = context.getTemplate();

        if (template.equippedItems().isEmpty()) {
            return StepResult.skip();
        }

        final Map<Layer, Integer> equippedItems = new HashMap<>();
        for (String itemName : template.equippedItems()) {
            final var request = ItemRequest.byName(itemName);
            final var target = ItemTarget.orphan();

            // The NPC serial is already allocated, although its mobile has not been created yet.
            final var item = itemModule.createItem(request, target);
            item.setCurrentLocation(ItemLocation.equipped(context.getSerialId()));

            equippedItems.put(item.getLayer(), item.getSerialId());
        }
        context.setEquippedItems(equippedItems);
        return StepResult.success();
    }
}
