package com.github.mayconr.juoserver.game.combat.flow.execution;

import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.CombatTypeResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.ResolveCombatMaxDistanceStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.resolver.WeaponResolverStep;
import com.github.mayconr.juoserver.game.combat.flow.execution.validation.ValidateTargetDistanceStep;
import com.github.mayconr.juoserver.game.world.context.FlowRegistryFactory;
import com.github.mayconr.juoserver.infrastructure.flow.Flow;
import com.github.mayconr.juoserver.infrastructure.flow.FlowFactory;

public class CombatExecutionFlow {
    private CombatExecutionFlow() {
    }

    public static Flow<CombatExecutionContext> build(FlowRegistryFactory.GameModules modules, FlowRegistryFactory.GameInfra infra) {
        return FlowFactory.<CombatExecutionContext>builder()
            .step(new WeaponResolverStep(infra.storage()))
            //
            // Identify combat type
            .step(new CombatTypeResolverStep())

            .step(new ResolveCombatMaxDistanceStep())
            .step(new ValidateTargetDistanceStep())
                // Calculate hit and anim frames
            //.step(new CalculateSwingFramesStep())
            // Sends anim event
            //.step(new BroadcastAttackAnimationStep(infra.eventBus()))
            // Wait for hit frame
            //.step(new CombatHitFrameDelayStep())
            //.step(new ApplyDamageStep(modules.damage()))
            .build();
    }

}
