package com.github.mayconr.juoserver.game.ai.definition.aggressive;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.PerceivePlayersStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.EvaluateSurvivalStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.FleeStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.RecoverStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.SelectTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.PursueTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.AttackTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.steps.IdleStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

/** Perceives, selects, pursues and requests attacks; survival remains a placeholder. */
public final class AggressiveAIDefinition {
    private AggressiveAIDefinition() {}

    public static Flow<AggressiveAIContext> build() {
        return FlowFactory.<AggressiveAIContext>builder()
                .step(new PerceivePlayersStep())
                .step(new EvaluateSurvivalStep())
                .step(new FleeStep())
                .step(new RecoverStep())
                .step(new SelectTargetStep())
                .step(new PursueTargetStep())
                .step(new AttackTargetStep())
                .step(new IdleStep())
                .build();
    }
}
