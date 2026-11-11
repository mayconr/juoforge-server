package com.github.mayconr.juoserver.game.item.flow.creation.template;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolves from the client tiledata whether the template graphic is stackable. Stackability belongs to the
 * graphic, not to the server template, because the client only renders and splits stacks of graphics with the
 * tiledata {@code GENERIC} flag. Without a file reader the item is treated as not stackable.
 */
@Slf4j
public class ResolveItemTileDataStep extends AbstractFlowStep<ItemCreationContext> {

    private final UOFileReader fileReader;

    public ResolveItemTileDataStep(UOFileReader fileReader) {
        super("resolve-item-tiledata");
        this.fileReader = fileReader;
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        var template = context.getTemplate();
        if (template == null) {
            return StepResult.failure("template is null");
        }
        context.setStackable(fileReader != null && fileReader.isStackable(template.modelId()));
        log.debug("Item {} model 0x{} stackable={}", template.name(),
                Integer.toHexString(template.modelId()).toUpperCase(), context.isStackable());
        return StepResult.success();
    }
}
