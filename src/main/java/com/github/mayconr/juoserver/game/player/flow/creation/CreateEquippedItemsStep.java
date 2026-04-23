package com.github.mayconr.juoserver.game.player.flow.creation;

import com.github.mayconr.juoserver.game.flow.PlayerCreationFlowDefinition.PlayerCreationContext;
import com.github.mayconr.juoserver.game.item.ItemModule;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.ItemTarget;
import com.github.mayconr.juoserver.game.model.Layer;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CreateEquippedItemsStep extends AbstractFlowStep<PlayerCreationContext> {

    private final ItemModule itemModule;

    public CreateEquippedItemsStep(ItemModule itemModule) {
        super("CreateStarterItems");
        this.itemModule = itemModule;
    }

    @Override
    public StepResult execute(PlayerCreationContext context) {
        Map<Layer, Integer> equippedItems = new HashMap<>();
        for (var req : context.getStartKitRequests()) {
            try {
                var item = itemModule.createItem(req, ItemTarget.orphan());
                if (item.hasFlag(ItemFlag.WEARABLE)) {
                    var layer = Objects.requireNonNull(item.getLayer(), "Layer is null");
                    equippedItems.put(layer, item.getSerialId());
                } else {
                    System.out.println("backpack "+item);
                }
            } catch (Exception exception) {
                log.error("Error while creating the item. Req: {} - {}", req, exception.getMessage());
            }
        }

        context.setEquippedItems(equippedItems);

        return StepResult.success();
    }
}
