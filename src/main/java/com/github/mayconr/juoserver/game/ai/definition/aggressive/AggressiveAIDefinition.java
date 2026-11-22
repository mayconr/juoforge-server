package com.github.mayconr.juoserver.game.ai.definition.aggressive;

import com.github.mayconr.juoserver.game.ai.definition.aggressive.perception.PerceivePlayersStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.survival.EvaluateSurvivalStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.survival.FleeStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.survival.RecoverStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.targeting.SelectTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.PursueTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.CheckPursuitMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.CalculatePursuitPathStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.EnqueuePursuitMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.pursuit.EnqueuePursuitPathMovementStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.attack.AttackTargetStep;
import com.github.mayconr.juoserver.game.ai.definition.aggressive.idle.IdleStep;
import com.github.mayconr.juoserver.infrastructure.datafile.UOFileReader;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowBuilder;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

/** Perceives, selects, pursues and requests attacks; survival remains a placeholder. */
public final class AggressiveAIDefinition {
    private AggressiveAIDefinition() {}

    public static Flow<AggressiveAIContext> build(UOFileReader fileReader) {
        return FlowFactory.<AggressiveAIContext>builder()
                .step(new PerceivePlayersStep())
                .step(new EvaluateSurvivalStep())
                .step(new FleeStep())
                .step(new RecoverStep())
                .step(new SelectTargetStep())
                .appendGroup("Pursuit", pursuit(fileReader))
                .step(new AttackTargetStep())
                .step(new IdleStep())
                .build();
    }

    private static FlowBuilder<AggressiveAIContext> pursuit(UOFileReader fileReader) {
        return FlowFactory.<AggressiveAIContext>builder()
                .step(new PursueTargetStep())
                .step(new CheckPursuitMovementStep(fileReader))
                .step(new CalculatePursuitPathStep(fileReader), context ->
                        context.getPursuit().isMovementBlocked()
                                && !context.getPursuit().hasPathFor(context.npc(), context.getTarget()))
                .step(new EnqueuePursuitPathMovementStep(), context -> context.getPursuit().isMovementBlocked())
                .step(new EnqueuePursuitMovementStep());
    }
}
