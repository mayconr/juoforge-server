package com.github.mayconr.juoserver.game.ai.definition.combat;

import com.github.mayconr.juoserver.game.ai.definition.combat.steps.PerceivePlayersStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.EvaluateSurvivalStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.FleeStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.RecoverStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.SelectTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.PursueTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.AttackTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.combat.steps.IdleStep;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

/** Perceives, selects, pursues and requests attacks; survival remains a placeholder. */
public final class CombatAIDefinition {
    private CombatAIDefinition() {}

    public static Flow<CombatAIContext> build() {
        return FlowFactory.<CombatAIContext>builder()
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
