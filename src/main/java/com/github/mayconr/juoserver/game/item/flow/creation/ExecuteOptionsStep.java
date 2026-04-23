package com.github.mayconr.juoserver.game.item.flow.creation;

import com.github.mayconr.juoserver.game.flow.ItemCreationFlowDefinition.ItemCreationContext;
import com.github.mayconr.juoserver.game.model.ItemCreationOptions;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecuteOptionsStep extends AbstractFlowStep<ItemCreationContext> {
    public ExecuteOptionsStep() {
        super("execute-options");
    }

    @Override
    public StepResult execute(ItemCreationContext context) {
        final var consumer = context.getConsumerOptions();

        final var options = new ItemCreationOptions();
        if (consumer != null) {
            consumer.accept(options);
        }
        context.setOptions(options);
        return StepResult.success();
    }
}
