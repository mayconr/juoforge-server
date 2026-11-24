package com.github.mayconr.juoserver.game.ai.definition.animal.flee;

import com.github.mayconr.juoserver.game.ai.actions.SpeechAction;
import com.github.mayconr.juoserver.game.ai.definition.animal.PassiveAnimalAIContext;
import com.github.mayconr.juoserver.game.model.event.message.MessageContent;
import com.github.mayconr.juoserver.infrastructure.flow.AbstractFlowStep;
import com.github.mayconr.juoserver.infrastructure.flow.StepResult;

public final class AlertAnimalStep extends AbstractFlowStep<PassiveAnimalAIContext> {

    public AlertAnimalStep() {
        super("AlertAnimalStep");
    }

    @Override
    public StepResult execute(PassiveAnimalAIContext context) {
        context.enqueueAction(new SpeechAction(
                context.npc(),
                context.flee().nearbyPlayer(),
                MessageContent.plain("*growls*")
        ));
        context.flee().markAlerted();
        return StepResult.success();
    }
}
