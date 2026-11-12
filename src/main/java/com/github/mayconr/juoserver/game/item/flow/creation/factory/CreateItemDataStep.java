package com.github.mayconr.juoserver.game.item.flow.creation.factory;

import com.github.mayconr.juoserver.game.item.ItemRequest;
import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.ItemFlag;
import com.github.mayconr.juoserver.game.model.UOItemData;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CreateItemDataStep extends AbstractFlowStep<ItemCreationContext> {

    private final SerialGenerator serialGenerator;

    public CreateItemDataStep(SerialGenerator serialGenerator) {
        super("CreateItemData");
        this.serialGenerator = serialGenerator;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {

        final var template = context.getTemplate();
        if (template == null) {
            return StepResult.failure("template is null");
        }

        var data = template.toData(serialGenerator.getNextItem());
        applyRequest(data, context.getRequest());
        data.setFlags(withStackable(data.getFlags(), context.isStackable()));

        context.setData(data);

        return StepResult.success();
    }

    /** Overrides the template defaults with the values requested for this item. */
    private static void applyRequest(UOItemData data, ItemRequest request) {
        if (request.hue() != null) {
            data.setHue(request.hue());
        }
        if (request.material() != null) {
            data.setMaterial(request.material());
        }
        data.setAmount(request.amount());
        data.setDirection(request.direction());
    }

    /** STACKABLE comes from the client tiledata resolved into the context, never from the template. */
    private static List<ItemFlag> withStackable(List<ItemFlag> templateFlags, boolean stackable) {
        var flags = new ArrayList<ItemFlag>(templateFlags == null ? List.of() : templateFlags);
        flags.remove(ItemFlag.STACKABLE);
        if (stackable) {
            flags.add(ItemFlag.STACKABLE);
        }
        return flags;
    }
}
