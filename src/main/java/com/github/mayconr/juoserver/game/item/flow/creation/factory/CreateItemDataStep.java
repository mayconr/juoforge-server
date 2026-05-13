package com.github.mayconr.juoserver.game.item.flow.creation.factory;

import com.github.mayconr.juoserver.game.item.flow.creation.ItemCreationContext;
import com.github.mayconr.juoserver.game.world.SerialGenerator;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

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

        final var request = context.getRequest();

        var data = template.toData(serialGenerator.getNextItem());

        // Override template defaults
        data.setHue(request.hue());
        data.setAmount(request.amount());
        data.setDirection(request.direction());


        context.setData(data);

        return StepResult.success();
    }
}
