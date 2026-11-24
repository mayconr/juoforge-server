package com.github.mayconr.juoserver.game.ai.definition.animal;

import com.github.mayconr.juoserver.game.ai.definition.animal.flee.AlertAnimalStep;
import com.github.mayconr.juoserver.game.ai.definition.animal.flee.DetectNearbyPlayerStep;
import com.github.mayconr.juoserver.game.ai.definition.animal.flee.FleeFromPlayerStep;
import com.github.mayconr.juoserver.game.ai.definition.shared.steps.WanderStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class PassiveAnimalAIDefinition {

    public static Flow<PassiveAnimalAIContext> build() {
        return FlowFactory.<PassiveAnimalAIContext>builder()
                .appendGroup("FleeBehavior", fleeBehavior())
                .step(new WanderStep<>(2.0), context -> context.flee().nearbyPlayer() == null)
                .build();
    }

    private static FlowBuilder<PassiveAnimalAIContext> fleeBehavior() {
        return FlowFactory.<PassiveAnimalAIContext>builder()
                .step(new DetectNearbyPlayerStep())
                .step(new AlertAnimalStep(), context -> context.flee().shouldAlert())
                .step(new FleeFromPlayerStep(), context -> context.flee().shouldFlee());
    }
}
